"""JadwalStudio local API. Run: py backend/server.py --host 127.0.0.1 --port 8765.

The bundled HTTP runner is for local development. For deployment use a WSGI
server behind HTTPS; the same application and SQLite database can be reused.
"""
import argparse
import base64
import binascii
import datetime as dt
import hashlib
import hmac
import io
import json
import re
import secrets
import sqlite3
import threading
import time
import uuid
import zipfile
from contextlib import contextmanager
from pathlib import Path
from urllib.parse import urlparse
from wsgiref.simple_server import make_server, WSGIRequestHandler

MAX_FILE = 10 * 1024 * 1024
STATUSES = ('To Do', 'In Progress', 'Review', 'Done')
DAYS = ('Senin', 'Selasa', 'Rabu', 'Kamis', 'Jumat')


class ApiError(Exception):
    def __init__(self, message, status=400):
        self.status = status
        super().__init__(message)


def required(data, key, maximum=200):
    value = data.get(key)
    if not isinstance(value, str) or not value.strip() or len(value) > maximum:
        raise ApiError(f'{key}: wajib diisi (maksimal {maximum} karakter).')
    return value.strip()


def optional(data, key, maximum=10000):
    value = data.get(key, '')
    if not isinstance(value, str) or len(value) > maximum:
        raise ApiError(f'{key}: teks tidak valid.')
    return value.strip()


def date_value(value, allow_empty=True):
    if value == '' and allow_empty:
        return ''
    try:
        if not re.fullmatch(r'\d{4}-\d{2}-\d{2}', value):
            raise ValueError()
        dt.date.fromisoformat(value)
        return value
    except (ValueError, TypeError):
        raise ApiError('Tanggal harus valid dengan format YYYY-MM-DD.')


def clock_value(value):
    if not isinstance(value, str) or not re.fullmatch(r'(?:[01]\d|2[0-3]):[0-5]\d', value):
        raise ApiError('Jam harus valid dengan format HH:mm (00:00–23:59).')
    return value


def identifier():
    return str(uuid.uuid4())


def token_hash(token):
    return hashlib.sha256(token.encode()).hexdigest()


class Store:
    def __init__(self, path):
        self.path = str(path)
        Path(path).parent.mkdir(parents=True, exist_ok=True)
        with self.connect() as db:
            db.executescript('''
                PRAGMA journal_mode=WAL;
                CREATE TABLE IF NOT EXISTS users (
                    id TEXT PRIMARY KEY, name TEXT NOT NULL, email TEXT NOT NULL UNIQUE,
                    salt TEXT NOT NULL, password_hash TEXT NOT NULL, created_at INTEGER NOT NULL);
                CREATE TABLE IF NOT EXISTS sessions (
                    token_hash TEXT PRIMARY KEY, user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                    expires_at INTEGER NOT NULL);
                CREATE TABLE IF NOT EXISTS subjects (
                    id TEXT PRIMARY KEY, user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                    name TEXT NOT NULL, name_key TEXT NOT NULL, UNIQUE(user_id, name_key));
                CREATE TABLE IF NOT EXISTS groups (
                    id TEXT PRIMARY KEY, name TEXT NOT NULL, description TEXT NOT NULL,
                    owner_id TEXT NOT NULL REFERENCES users(id), invite_hash TEXT NOT NULL UNIQUE,
                    created_at INTEGER NOT NULL);
                CREATE TABLE IF NOT EXISTS members (
                    group_id TEXT NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
                    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                    PRIMARY KEY(group_id, user_id));
                CREATE TABLE IF NOT EXISTS tasks (
                    id TEXT PRIMARY KEY, subject_id TEXT NOT NULL REFERENCES subjects(id),
                    title TEXT NOT NULL, description TEXT NOT NULL, notes TEXT NOT NULL,
                    deadline TEXT NOT NULL, priority INTEGER NOT NULL, status TEXT NOT NULL,
                    created_by TEXT NOT NULL REFERENCES users(id),
                    group_id TEXT REFERENCES groups(id) ON DELETE CASCADE,
                    created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL);
                CREATE TABLE IF NOT EXISTS task_members (
                    task_id TEXT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
                    user_id TEXT NOT NULL REFERENCES users(id), PRIMARY KEY(task_id,user_id));
                CREATE TABLE IF NOT EXISTS schedules (
                    id TEXT PRIMARY KEY, user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                    subject_id TEXT NOT NULL REFERENCES subjects(id), day TEXT NOT NULL,
                    start_time TEXT NOT NULL, end_time TEXT NOT NULL, room TEXT NOT NULL,
                    teacher TEXT NOT NULL, note TEXT NOT NULL);
                CREATE TABLE IF NOT EXISTS logs (
                    id TEXT PRIMARY KEY, group_id TEXT NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
                    created_by TEXT NOT NULL REFERENCES users(id), date TEXT NOT NULL,
                    attendees TEXT NOT NULL, decisions TEXT NOT NULL, action_items TEXT NOT NULL,
                    next_meeting_date TEXT NOT NULL);
                CREATE TABLE IF NOT EXISTS attachments (
                    id TEXT PRIMARY KEY, task_id TEXT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
                    name TEXT NOT NULL, mime TEXT NOT NULL, size INTEGER NOT NULL,
                    content BLOB NOT NULL, uploaded_by TEXT NOT NULL REFERENCES users(id));
                CREATE INDEX IF NOT EXISTS tasks_group ON tasks(group_id);
                CREATE INDEX IF NOT EXISTS tasks_creator ON tasks(created_by);
                CREATE INDEX IF NOT EXISTS schedules_owner_day ON schedules(user_id,day,start_time);
                CREATE INDEX IF NOT EXISTS memberships_user ON members(user_id);
                CREATE TABLE IF NOT EXISTS profile_photos (
                    user_id TEXT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                    photo TEXT NOT NULL);
            ''')

    @contextmanager
    def connect(self):
        db = sqlite3.connect(self.path, timeout=15)
        db.row_factory = sqlite3.Row
        db.execute('PRAGMA foreign_keys=ON')
        try:
            with db:
                yield db
        finally:
            db.close()

    def dispatch(self, method, path, data=None, token=''):
        data = {} if data is None else data
        if not isinstance(data, dict):
            raise ApiError('Body harus berupa objek JSON.')
        with self.connect() as db:
            # Serialize validations and writes: no concurrent overlapping schedules,
            # duplicate memberships, or partially committed assignment changes.
            db.execute('BEGIN IMMEDIATE' if method != 'GET' else 'BEGIN')
            if path in ('/auth/register', '/auth/login') and method == 'POST':
                email = required(data, 'email', 254).casefold()
                if not re.fullmatch(r'[^\s@]+@[^\s@]+\.[^\s@]+', email):
                    raise ApiError('Format email tidak valid.')
                password = data.get('password', '')
                if not isinstance(password, str) or not 8 <= len(password) <= 128:
                    raise ApiError('Password harus 8–128 karakter.')
                if path.endswith('register'):
                    name = required(data, 'name', 100)
                    if data.get('confirmPassword') != password:
                        raise ApiError('Konfirmasi password tidak sama.')
                    salt = secrets.token_hex(16)
                    hashed = hashlib.pbkdf2_hmac('sha256', password.encode(), bytes.fromhex(salt), 600000).hex()
                    try:
                        db.execute('INSERT INTO users VALUES (?,?,?,?,?,?)',
                                   (identifier(), name, email, salt, hashed, int(time.time())))
                    except sqlite3.IntegrityError:
                        raise ApiError('Email sudah terdaftar.', 409)
                    return {'message': 'Akun berhasil dibuat. Silakan masuk.'}
                user = db.execute('SELECT * FROM users WHERE email=?', (email,)).fetchone()
                salt = user['salt'] if user else '00' * 16
                hashed = hashlib.pbkdf2_hmac('sha256', password.encode(), bytes.fromhex(salt), 600000).hex()
                if not user or not hmac.compare_digest(hashed, user['password_hash']):
                    raise ApiError('Email atau password salah.', 401)
                new_token = secrets.token_urlsafe(32)
                db.execute('DELETE FROM sessions WHERE expires_at<=?', (int(time.time()),))
                db.execute('INSERT INTO sessions VALUES (?,?,?)',
                           (token_hash(new_token), user['id'], int(time.time()) + 30 * 86400))
                return {'token': new_token, 'user': dict(id=user['id'], name=user['name'], email=user['email'])}

            session = db.execute('SELECT user_id FROM sessions WHERE token_hash=? AND expires_at>?',
                                 (token_hash(token), int(time.time()))).fetchone()
            if not session:
                raise ApiError('Sesi berakhir. Silakan masuk kembali.', 401)
            uid = session['user_id']
            parts = path.strip('/').split('/')
            if path == '/auth/logout' and method == 'POST':
                db.execute('DELETE FROM sessions WHERE token_hash=?', (token_hash(token),))
                return {'message': 'Berhasil keluar.'}
            if path == '/state' and method == 'GET':
                return self.state(db, uid)
            if path == '/profile/photo' and method == 'PUT':
                photo = data.get('photo')
                if not isinstance(photo, str) or len(photo) > 700000:
                    raise ApiError('Foto terlalu besar. Maksimal 500 KB.')
                try:
                    raw = base64.b64decode(photo, validate=True)
                except (ValueError, binascii.Error):
                    raise ApiError('Format foto tidak valid.')
                if len(raw) > 512000 or not raw.startswith(b'\xff\xd8\xff') or not raw.endswith(b'\xff\xd9'):
                    raise ApiError('Gunakan foto JPEG yang valid, maksimal 500 KB.')
                db.execute('INSERT OR REPLACE INTO profile_photos VALUES (?,?)', (uid, photo))
                return {'message': 'Foto profil disimpan.'}
            if parts[0] == 'subjects':
                if method == 'POST' and len(parts) == 1:
                    name = required(data, 'name', 100)
                    sid = identifier()
                    try:
                        db.execute('INSERT INTO subjects VALUES (?,?,?,?)', (sid, uid, name, name.casefold()))
                    except sqlite3.IntegrityError:
                        raise ApiError('Mata pelajaran sudah ada.', 409)
                    return {'id': sid}
                if method == 'DELETE' and len(parts) == 2:
                    self.subject(db, uid, parts[1])
                    if db.execute('SELECT 1 FROM tasks WHERE subject_id=? AND group_id IS NOT NULL', (parts[1],)).fetchone():
                        raise ApiError('Mata pelajaran masih dipakai tugas kelompok. Hapus tugas tersebut dahulu.', 409)
                    db.execute('DELETE FROM tasks WHERE subject_id=?', (parts[1],))
                    db.execute('DELETE FROM schedules WHERE subject_id=?', (parts[1],))
                    db.execute('DELETE FROM subjects WHERE id=?', (parts[1],))
                    return {'ok': True}
            if parts[0] == 'schedules':
                sid = parts[1] if len(parts) == 2 else identifier()
                if len(parts) == 2:
                    if not db.execute('SELECT 1 FROM schedules WHERE id=? AND user_id=?', (sid, uid)).fetchone():
                        raise ApiError('Jadwal tidak ditemukan.', 404)
                if method == 'DELETE' and len(parts) == 2:
                    db.execute('DELETE FROM schedules WHERE id=?', (sid,))
                    return {'ok': True}
                if (method == 'POST' and len(parts) == 1) or (method == 'PUT' and len(parts) == 2):
                    subject = required(data, 'subjectId')
                    self.subject(db, uid, subject)
                    day = required(data, 'day')
                    start, end = clock_value(data.get('startTime')), clock_value(data.get('endTime'))
                    if day not in DAYS or end <= start:
                        raise ApiError('Pilih Senin–Jumat dan jam selesai setelah jam mulai.')
                    if db.execute('SELECT 1 FROM schedules WHERE user_id=? AND day=? AND id<>? AND start_time<? AND end_time>?',
                                  (uid, day, sid, end, start)).fetchone():
                        raise ApiError('Jadwal bentrok dengan pelajaran lain pada hari yang sama.', 409)
                    db.execute('INSERT OR REPLACE INTO schedules VALUES (?,?,?,?,?,?,?,?,?)',
                               (sid, uid, subject, day, start, end, optional(data, 'room', 100), optional(data, 'teacher', 100), optional(data, 'note')))
                    return {'id': sid}
            if parts[0] == 'groups':
                if method == 'POST' and len(parts) == 1:
                    gid, invite = identifier(), secrets.token_urlsafe(24)
                    db.execute('INSERT INTO groups VALUES (?,?,?,?,?,?)',
                               (gid, required(data, 'name', 100), optional(data, 'description'), uid, token_hash(invite), int(time.time())))
                    db.execute('INSERT INTO members VALUES (?,?)', (gid, uid))
                    return {'id': gid, 'inviteToken': invite}
                if path == '/groups/join' and method == 'POST':
                    invite = required(data, 'token', 1000)
                    if '/' in invite:
                        invite = urlparse(invite).path.rstrip('/').split('/')[-1]
                    group = db.execute('SELECT * FROM groups WHERE invite_hash=?', (token_hash(invite),)).fetchone()
                    if not group:
                        raise ApiError('Link atau kode undangan tidak valid.', 404)
                    try:
                        db.execute('INSERT INTO members VALUES (?,?)', (group['id'], uid))
                    except sqlite3.IntegrityError:
                        raise ApiError('Kamu sudah menjadi anggota kelompok ini.', 409)
                    return {'id': group['id']}
                if len(parts) >= 2:
                    gid = parts[1]
                    group = self.group(db, uid, gid)
                    if len(parts) == 3 and parts[2] == 'invite' and method == 'POST':
                        self.owner(group, uid)
                        invite = secrets.token_urlsafe(24)
                        db.execute('UPDATE groups SET invite_hash=? WHERE id=?', (token_hash(invite), gid))
                        return {'inviteToken': invite}
                    if len(parts) == 4 and parts[2] == 'members' and method == 'DELETE':
                        self.owner(group, uid)
                        member = parts[3]
                        if member == uid:
                            raise ApiError('Owner tidak dapat dihapus dari kelompok.')
                        db.execute('DELETE FROM task_members WHERE user_id=? AND task_id IN (SELECT id FROM tasks WHERE group_id=?)', (member, gid))
                        # Keep each group task assigned even when its only assignee leaves.
                        db.execute('INSERT INTO task_members SELECT id,? FROM tasks t WHERE group_id=? AND NOT EXISTS (SELECT 1 FROM task_members m WHERE m.task_id=t.id)', (uid, gid))
                        db.execute('DELETE FROM members WHERE group_id=? AND user_id=?', (gid, member))
                        return {'ok': True}
                    if len(parts) == 3 and parts[2] == 'logs' and method == 'POST':
                        lid = identifier()
                        db.execute('INSERT INTO logs VALUES (?,?,?,?,?,?,?,?)',
                                   (lid, gid, uid, date_value(data.get('date'), False), required(data, 'attendees', 2000),
                                    required(data, 'decisions', 10000), required(data, 'actionItems', 10000),
                                    date_value(data.get('nextMeetingDate', ''))))
                        return {'id': lid}
            if parts[0] == 'logs' and len(parts) == 2 and method == 'DELETE':
                row = db.execute('SELECT * FROM logs WHERE id=?', (parts[1],)).fetchone()
                if not row:
                    raise ApiError('Catatan tidak ditemukan.', 404)
                group = self.group(db, uid, row['group_id'])
                if row['created_by'] != uid and group['owner_id'] != uid:
                    raise ApiError('Hanya pembuat atau owner boleh menghapus catatan.', 403)
                db.execute('DELETE FROM logs WHERE id=?', (parts[1],))
                return {'ok': True}
            if parts[0] == 'tasks':
                if len(parts) == 1 and method == 'POST':
                    return self.create_task(db, uid, data)
                if len(parts) >= 2:
                    task = self.task(db, uid, parts[1])
                    if len(parts) == 2 and method == 'PUT':
                        self.can_edit_task(db, uid, task)
                        status = data.get('status', task['status'])
                        if status not in STATUSES:
                            raise ApiError('Status tugas tidak valid.')
                        if 'assigneeIds' in data:
                            self.assign(db, task['id'], task['group_id'], task['created_by'], data['assigneeIds'])
                        if 'attachment' in data and data['attachment'] is not None:
                            self.attachment(db, uid, task['id'], data['attachment'])
                        db.execute('UPDATE tasks SET status=?,description=?,notes=?,updated_at=? WHERE id=?',
                                   (status, optional(data, 'description') if 'description' in data else task['description'],
                                    optional(data, 'notes') if 'notes' in data else task['notes'], int(time.time()), task['id']))
                        return {'id': task['id']}
                    if len(parts) == 2 and method == 'DELETE':
                        self.can_edit_task(db, uid, task)
                        db.execute('DELETE FROM tasks WHERE id=?', (task['id'],))
                        return {'ok': True}
                    if len(parts) == 3 and parts[2] == 'attachments' and method == 'POST':
                        self.can_edit_task(db, uid, task)
                        return self.attachment(db, uid, task['id'], data)
            if parts[0] == 'attachments' and len(parts) == 2 and method == 'GET':
                row = db.execute('SELECT * FROM attachments WHERE id=?', (parts[1],)).fetchone()
                if not row:
                    raise ApiError('Lampiran tidak ditemukan.', 404)
                self.task(db, uid, row['task_id'])
                return {'name': row['name'], 'mime': row['mime'], 'content': base64.b64encode(row['content']).decode()}
            raise ApiError('Endpoint tidak ditemukan.', 404)

    def subject(self, db, uid, sid):
        if not db.execute('SELECT 1 FROM subjects WHERE id=? AND user_id=?', (sid, uid)).fetchone():
            raise ApiError('Pilih mata pelajaran milikmu.', 403)

    def group(self, db, uid, gid):
        row = db.execute('SELECT g.* FROM groups g JOIN members m ON m.group_id=g.id WHERE g.id=? AND m.user_id=?', (gid, uid)).fetchone()
        if not row:
            raise ApiError('Kelompok tidak ditemukan atau kamu bukan anggota.', 403)
        return row

    def owner(self, group, uid):
        if group['owner_id'] != uid:
            raise ApiError('Tindakan ini hanya untuk owner.', 403)

    def task(self, db, uid, tid):
        row = db.execute('SELECT * FROM tasks WHERE id=?', (tid,)).fetchone()
        if not row:
            raise ApiError('Tugas tidak ditemukan.', 404)
        if row['group_id']:
            self.group(db, uid, row['group_id'])
        elif row['created_by'] != uid:
            raise ApiError('Tugas bukan milikmu.', 403)
        return row

    def can_edit_task(self, db, uid, task):
        if task['created_by'] == uid:
            return
        if task['group_id']:
            group = self.group(db, uid, task['group_id'])
            if group['owner_id'] == uid or db.execute('SELECT 1 FROM task_members WHERE task_id=? AND user_id=?', (task['id'], uid)).fetchone():
                return
        raise ApiError('Hanya owner, pembuat, atau anggota yang ditugaskan boleh mengubah tugas.', 403)

    def assign(self, db, tid, gid, creator, assignees):
        if not isinstance(assignees, list) or not assignees or any(not isinstance(x, str) for x in assignees):
            raise ApiError('Pilih minimal satu anggota untuk tugas.')
        assignees = list(dict.fromkeys(assignees))
        if not gid and assignees != [creator]:
            raise ApiError('Tugas individu harus ditugaskan kepada pembuatnya.')
        if gid:
            for assignee in assignees:
                if not db.execute('SELECT 1 FROM members WHERE group_id=? AND user_id=?', (gid, assignee)).fetchone():
                    raise ApiError('Penerima tugas harus anggota kelompok.', 403)
        db.execute('DELETE FROM task_members WHERE task_id=?', (tid,))
        db.executemany('INSERT INTO task_members VALUES (?,?)', [(tid, x) for x in assignees])

    def create_task(self, db, uid, data):
        subject = required(data, 'subjectId')
        self.subject(db, uid, subject)
        gid = data.get('groupId')
        if gid is not None and not isinstance(gid, str):
            raise ApiError('groupId harus berupa teks atau null.')
        gid = gid or None
        if gid:
            self.group(db, uid, gid)
        tid, now = identifier(), int(time.time())
        priority = data.get('priority', False)
        if not isinstance(priority, bool):
            raise ApiError('Prioritas tidak valid.')
        db.execute('INSERT INTO tasks VALUES (?,?,?,?,?,?,?,?,?,?,?,?)',
                   (tid, subject, required(data, 'title'), optional(data, 'description'), optional(data, 'notes'),
                    date_value(data.get('deadline', '')), int(priority), 'To Do', uid, gid, now, now))
        self.assign(db, tid, gid, uid, data.get('assigneeIds', [uid]))
        if 'attachment' in data and data['attachment'] is not None:
            self.attachment(db, uid, tid, data['attachment'])
        return {'id': tid}

    def attachment(self, db, uid, tid, data):
        if not isinstance(data, dict):
            raise ApiError('Lampiran harus berupa objek JSON.')
        name = required(data, 'name', 200).replace('\\', '/').split('/')[-1]
        try:
            raw = base64.b64decode(data.get('content', ''), validate=True)
        except (ValueError, TypeError):
            raise ApiError('Isi lampiran tidak valid.')
        if not 0 < len(raw) <= MAX_FILE:
            raise ApiError('Lampiran harus berisi data dan maksimal 10 MB.')
        ext = Path(name).suffix.lower()
        valid = False
        mime = ''
        if ext == '.pdf':
            valid, mime = raw.startswith(b'%PDF-'), 'application/pdf'
        elif ext == '.png':
            valid, mime = raw.startswith(b'\x89PNG\r\n\x1a\n'), 'image/png'
        elif ext in ('.jpg', '.jpeg'):
            valid, mime = raw.startswith(b'\xff\xd8\xff'), 'image/jpeg'
        elif ext == '.docx':
            try:
                with zipfile.ZipFile(io.BytesIO(raw)) as archive:
                    names = archive.namelist()
                    valid = '[Content_Types].xml' in names and 'word/document.xml' in names
                mime = 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
            except (zipfile.BadZipFile, OSError):
                pass
        if not valid:
            raise ApiError('Gunakan PDF, JPG, PNG, atau DOCX yang valid.')
        aid = identifier()
        db.execute('INSERT INTO attachments VALUES (?,?,?,?,?,?,?)', (aid, tid, name, mime, len(raw), raw, uid))
        return {'id': aid}

    def state(self, db, uid):
        user = dict(db.execute('SELECT id,name,email FROM users WHERE id=?', (uid,)).fetchone())
        photo = db.execute('SELECT photo FROM profile_photos WHERE user_id=?', (uid,)).fetchone()
        user['photo'] = photo['photo'] if photo else ''
        groups = [dict(r) for r in db.execute('SELECT g.id,g.name,g.description,g.owner_id,g.created_at FROM groups g JOIN members m ON m.group_id=g.id WHERE m.user_id=?', (uid,))]
        for group in groups:
            group['members'] = [dict(r) for r in db.execute('''SELECT u.id,u.name,u.email,
                (SELECT COUNT(*) FROM task_members tm JOIN tasks t ON t.id=tm.task_id WHERE tm.user_id=u.id AND t.group_id=?) AS assigned,
                (SELECT COUNT(*) FROM task_members tm JOIN tasks t ON t.id=tm.task_id WHERE tm.user_id=u.id AND t.group_id=? AND t.status='Done') AS finished
                FROM users u JOIN members m ON m.user_id=u.id WHERE m.group_id=?''', (group['id'], group['id'], group['id']))]
            for member in group['members']:
                member['role'] = 'owner' if member['id'] == group['owner_id'] else 'member'
                member['contribution'] = round(member['finished'] * 100 / member['assigned']) if member['assigned'] else 0
        tasks = [dict(r) for r in db.execute('''SELECT t.*,s.name AS subject FROM tasks t JOIN subjects s ON s.id=t.subject_id
            WHERE (t.group_id IS NULL AND t.created_by=?) OR t.group_id IN (SELECT group_id FROM members WHERE user_id=?)
            ORDER BY t.created_at DESC,t.id''', (uid, uid))]
        for task in tasks:
            task['assigneeIds'] = [r[0] for r in db.execute('SELECT user_id FROM task_members WHERE task_id=?', (task['id'],))]
            task['attachments'] = [dict(r) for r in db.execute('SELECT id,name,mime,size FROM attachments WHERE task_id=?', (task['id'],))]
        return dict(user=user, subjects=[dict(r) for r in db.execute('SELECT id,name FROM subjects WHERE user_id=? ORDER BY name_key', (uid,))],
                    groups=groups, tasks=tasks,
                    schedules=[dict(r) for r in db.execute('SELECT sc.*,s.name AS subject FROM schedules sc JOIN subjects s ON s.id=sc.subject_id WHERE sc.user_id=? ORDER BY sc.start_time,sc.id', (uid,))],
                    logs=[dict(r) for r in db.execute('SELECT * FROM logs WHERE group_id IN (SELECT group_id FROM members WHERE user_id=?) ORDER BY date DESC,id', (uid,))])


class Application:
    def __init__(self, store):
        self.store = store
        self.attempts = {}
        self.lock = threading.Lock()

    def __call__(self, env, start_response):
        status = 200
        try:
            method, path = env['REQUEST_METHOD'], env['PATH_INFO']
            if path == '/health' and method == 'GET':
                result = {'ok': True}
            elif path.startswith('/join/') and method == 'GET':
                result = {'message': 'Buka JadwalStudio > Tugas > Gabung kelompok, lalu tempel link ini.'}
            else:
                if path.startswith('/auth/') and path != '/auth/logout':
                    with self.lock:
                        now = time.time()
                        self.attempts = {k: [t for t in v if now-t < 60] for k, v in self.attempts.items() if v and now-v[-1] < 60}
                        attempts = self.attempts.setdefault(env.get('REMOTE_ADDR', ''), [])
                        if len(attempts) >= 15:
                            raise ApiError('Terlalu banyak percobaan. Tunggu satu menit.', 429)
                        attempts.append(now)
                try:
                    length = int(env.get('CONTENT_LENGTH') or 0)
                except (ValueError, TypeError):
                    raise ApiError('Content-Length tidak valid.')
                if not 0 <= length <= 15 * 1024 * 1024:
                    raise ApiError('Ukuran permintaan terlalu besar.', 413)
                raw = env['wsgi.input'].read(length)
                try:
                    data = json.loads(raw) if raw else {}
                except (ValueError, UnicodeDecodeError):
                    raise ApiError('JSON tidak valid.')
                auth = env.get('HTTP_AUTHORIZATION', '')
                token = auth[7:] if auth.startswith('Bearer ') else ''
                result = self.store.dispatch(method, path, data, token)
        except ApiError as error:
            status, result = error.status, {'error': str(error)}
        except Exception:
            # Do not leak request bodies, passwords, tokens, or database details.
            status, result = 500, {'error': 'Server gagal memproses permintaan. Coba lagi.'}
        payload = json.dumps(result, ensure_ascii=False).encode()
        reason = {200:'OK',400:'Bad Request',401:'Unauthorized',403:'Forbidden',404:'Not Found',409:'Conflict',413:'Payload Too Large',429:'Too Many Requests',500:'Internal Server Error'}[status]
        start_response(f'{status} {reason}', [('Content-Type','application/json; charset=utf-8'), ('Content-Length',str(len(payload))), ('Cache-Control','no-store'), ('X-Content-Type-Options','nosniff')])
        return [payload]


class QuietHandler(WSGIRequestHandler):
    def log_message(self, format, *args):
        # URLs may contain invitation tokens; keep them out of access logs.
        pass


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--host', default='127.0.0.1')
    parser.add_argument('--port', default=8765, type=int)
    parser.add_argument('--db', default=str(Path(__file__).parent / 'data' / 'jadwalstudio.sqlite3'))
    args = parser.parse_args()
    app = Application(Store(args.db))
    print(f'JadwalStudio local API: http://{args.host}:{args.port}', flush=True)
    with make_server(args.host, args.port, app, handler_class=QuietHandler) as server:
        server.serve_forever()
