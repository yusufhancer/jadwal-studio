import base64
import io
import json
import tempfile
import threading
import unittest
import urllib.request
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

from server import ApiError, Application, Store, QuietHandler
from wsgiref.simple_server import make_server


class ApiTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.path = Path(self.temp.name) / 'test.sqlite3'
        self.store = Store(self.path)
        self.owner, self.owner_id = self.account('Owner', 'owner@example.com')
        self.member, self.member_id = self.account('Member', 'member@example.com')
        self.outsider, self.outsider_id = self.account('Other', 'other@example.com')
        self.subject = self.call('POST', '/subjects', {'name': 'Matematika'})['id']
        group = self.call('POST', '/groups', {'name': 'Kelompok A'})
        self.group, self.invite = group['id'], group['inviteToken']
        self.call('POST', '/groups/join', {'token': self.invite}, self.member)

    def tearDown(self):
        self.temp.cleanup()

    def account(self, name, email):
        data = dict(name=name, email=email, password='Password123!', confirmPassword='Password123!')
        self.store.dispatch('POST', '/auth/register', data)
        result = self.store.dispatch('POST', '/auth/login', data)
        return result['token'], result['user']['id']

    def call(self, method, path, data=None, token=None):
        return self.store.dispatch(method, path, data, token if token is not None else self.owner)

    def denied(self, method, path, data=None, token=None, status=None):
        with self.assertRaises(ApiError) as caught:
            self.call(method, path, data, token)
        if status:
            self.assertEqual(status, caught.exception.status)

    def task(self, **changes):
        data = dict(title='Latihan', subjectId=self.subject, deadline='2030-09-28', priority=False)
        data.update(changes)
        return self.call('POST', '/tasks', data)['id']

    def schedule(self, **changes):
        data = dict(subjectId=self.subject, day='Senin', startTime='08:00', endTime='09:00')
        data.update(changes)
        return data

    def test_registration_login_logout_and_hashing(self):
        self.denied('POST', '/auth/register', dict(name='X', email='OWNER@example.com', password='Password123!', confirmPassword='Password123!'), status=409)
        self.denied('POST', '/auth/login', dict(email='owner@example.com', password='WrongPassword'), status=401)
        self.denied('POST', '/auth/register', dict(name='X', email='invalid', password='12345678', confirmPassword='12345678'))
        self.denied('POST', '/auth/register', dict(name='X', email='new@example.com', password='12345678', confirmPassword='87654321'))
        with self.store.connect() as db:
            row = db.execute('SELECT * FROM users WHERE id=?', (self.owner_id,)).fetchone()
            self.assertNotEqual(row['password_hash'], 'Password123!')
            self.assertEqual(64, len(row['password_hash']))
            self.assertEqual(32, len(row['salt']))
        self.call('POST', '/auth/logout')
        self.denied('GET', '/state', status=401)

    def test_profile_photo_persists_and_is_private_to_account(self):
        photo = base64.b64encode(b'\xff\xd8\xff\xe0test\xff\xd9').decode()
        self.call('PUT', '/profile/photo', {'photo': photo})
        self.store = Store(self.path)
        self.assertEqual(photo, self.call('GET', '/state')['user']['photo'])
        self.assertEqual('', self.call('GET', '/state', token=self.member)['user']['photo'])
        self.denied('PUT', '/profile/photo', {'photo': 'invalid'})
        self.denied('PUT', '/profile/photo', {'photo': base64.b64encode(b'not an image').decode()})
        self.denied('PUT', '/profile/photo', {'photo': 'a' * 700001})
        self.denied('PUT', '/profile/photo', {'photo': photo}, token='', status=401)

    def test_groups_visible_before_and_after_adding_tasks(self):
        self.assertIn(self.group, [g['id'] for g in self.call('GET', '/state')['groups']])
        self.task(groupId=self.group, assigneeIds=[self.owner_id])
        self.assertIn(self.group, [g['id'] for g in self.call('GET', '/state', token=self.member)['groups']])

    def test_persistence_and_personal_isolation(self):
        tid = self.task(description='Penjelasan', notes='Catatan')
        self.call('POST', '/schedules', self.schedule())
        self.store = Store(self.path)
        state = self.call('GET', '/state')
        self.assertEqual(tid, state['tasks'][0]['id'])
        self.assertEqual('Penjelasan', state['tasks'][0]['description'])
        self.assertEqual([self.owner_id], state['tasks'][0]['assigneeIds'])
        self.assertEqual('To Do', state['tasks'][0]['status'])
        self.assertEqual(1, len(state['schedules']))
        other = self.call('GET', '/state', token=self.outsider)
        for field in ('tasks', 'schedules', 'subjects', 'groups', 'logs'):
            self.assertEqual([], other[field])
        self.denied('PUT', f'/tasks/{tid}', {'status': 'Done'}, self.outsider, 403)

    def test_subject_uniqueness_ownership_and_deletion(self):
        self.denied('POST', '/subjects', {'name': ' matematika '}, status=409)
        self.denied('POST', '/subjects', {'name': '  '})
        self.denied('POST', '/tasks', {'title': 'X', 'subjectId': self.subject}, self.member, 403)
        self.task()
        self.call('POST', '/schedules', self.schedule())
        self.call('DELETE', f'/subjects/{self.subject}')
        state = self.call('GET', '/state')
        self.assertEqual([], state['tasks'])
        self.assertEqual([], state['schedules'])

    def test_schedule_overlap_edit_adjacency_and_sort(self):
        first = self.call('POST', '/schedules', self.schedule(startTime='09:00', endTime='10:00'))['id']
        second = self.call('POST', '/schedules', self.schedule())['id']
        self.denied('POST', '/schedules', self.schedule(startTime='08:30', endTime='09:30'), status=409)
        self.call('PUT', f'/schedules/{second}', self.schedule())
        self.denied('PUT', f'/schedules/{second}', self.schedule(endTime='09:01'), status=409)
        self.assertEqual(['08:00', '09:00'], [x['start_time'] for x in self.call('GET', '/state')['schedules']])
        self.denied('DELETE', f'/schedules/{first}', token=self.member, status=404)

    def test_invalid_times_and_dates(self):
        for start, end in [('25:00', '26:00'), ('08:60', '09:00'), ('08:00', '08:00'), ('9:00', '10:00'), ('', '09:00')]:
            self.denied('POST', '/schedules', self.schedule(startTime=start, endTime=end))
        self.denied('POST', '/schedules', self.schedule(day='Minggu'))
        for deadline in ('2030-02-30', 'besok', '2030-13-01', '2030-01-01oops'):
            self.denied('POST', '/tasks', {'subjectId': self.subject, 'title': 'X', 'deadline': deadline})

    def test_concurrent_schedule_insertion_is_atomic(self):
        barrier = threading.Barrier(2)
        def insert():
            barrier.wait()
            try:
                self.call('POST', '/schedules', self.schedule())
                return 'saved'
            except ApiError:
                return 'conflict'
        with ThreadPoolExecutor(max_workers=2) as pool:
            results = list(pool.map(lambda _: insert(), range(2)))
        self.assertCountEqual(['saved', 'conflict'], results)

    def test_invite_rotation_duplicates_and_owner_permission(self):
        self.denied('POST', '/groups/join', {'token': self.invite}, self.member, 409)
        self.denied('POST', f'/groups/{self.group}/invite', token=self.member, status=403)
        invite = self.call('POST', f'/groups/{self.group}/invite')['inviteToken']
        self.denied('POST', '/groups/join', {'token': self.invite}, self.outsider, 404)
        self.call('POST', '/groups/join', {'token': 'https://example.com/join/' + invite}, self.outsider)
        self.assertEqual(3, len(self.call('GET', '/state')['groups'][0]['members']))

    def test_assignments_status_permission_and_contribution(self):
        tid = self.task(groupId=self.group, assigneeIds=[self.owner_id, self.member_id])
        self.call('PUT', f'/tasks/{tid}', {'status': 'In Progress'}, self.member)
        self.call('PUT', f'/tasks/{tid}', {'status': 'Review', 'notes': 'Siap dinilai'}, self.member)
        self.call('PUT', f'/tasks/{tid}', {'status': 'Done'}, self.member)
        state = self.call('GET', '/state')
        for member in state['groups'][0]['members']:
            self.assertEqual(100, member['contribution'])
        self.denied('PUT', f'/tasks/{tid}', {'status': 'Done'}, self.outsider, 403)
        self.denied('PUT', f'/tasks/{tid}', {'assigneeIds': [self.outsider_id]}, status=403)
        self.denied('PUT', f'/tasks/{tid}', {'assigneeIds': []})
        self.denied('PUT', f'/tasks/{tid}', {'status': 'Overdue'})
        self.call('PUT', f'/tasks/{tid}', {'assigneeIds': [self.owner_id]})
        self.denied('PUT', f'/tasks/{tid}', {'status': 'To Do'}, self.member, 403)

    def test_remove_member_revokes_access_and_reassigns_orphan_task(self):
        tid = self.task(groupId=self.group, assigneeIds=[self.member_id])
        self.denied('DELETE', f'/groups/{self.group}/members/{self.owner_id}', token=self.member, status=403)
        self.denied('DELETE', f'/groups/{self.group}/members/{self.owner_id}')
        self.call('DELETE', f'/groups/{self.group}/members/{self.member_id}')
        self.assertEqual([self.owner_id], self.call('GET', '/state')['tasks'][0]['assigneeIds'])
        self.denied('PUT', f'/tasks/{tid}', {'status': 'Done'}, self.member, 403)
        self.assertEqual([], self.call('GET', '/state', token=self.member)['tasks'])

    def test_logs_scope_permission_and_validation(self):
        payload = dict(date='2030-09-28', attendees='Owner, Member', decisions='Membagi tugas', actionItems='Kerjakan UI', nextMeetingDate='2030-10-01')
        lid = self.call('POST', f'/groups/{self.group}/logs', payload, self.member)['id']
        self.assertEqual(lid, self.call('GET', '/state')['logs'][0]['id'])
        self.denied('POST', f'/groups/{self.group}/logs', payload, self.outsider, 403)
        self.denied('POST', f'/groups/{self.group}/logs', dict(payload, date=''))
        self.call('DELETE', f'/logs/{lid}')
        self.assertEqual([], self.call('GET', '/state')['logs'])

    def test_attachments_validation_authorization_and_transaction(self):
        raw = b'%PDF-1.4\nSample test file\n%%EOF'
        attachment = dict(name='bukti.pdf', content=base64.b64encode(raw).decode())
        tid = self.task(attachment=attachment)
        aid = self.call('GET', '/state')['tasks'][0]['attachments'][0]['id']
        self.assertEqual(raw, base64.b64decode(self.call('GET', f'/attachments/{aid}')['content']))
        self.denied('GET', f'/attachments/{aid}', token=self.member, status=403)
        invalid = dict(name='fake.pdf', content=base64.b64encode(b'not a PDF').decode())
        self.denied('PUT', f'/tasks/{tid}', {'status': 'Done', 'attachment': invalid})
        self.assertEqual('To Do', self.call('GET', '/state')['tasks'][0]['status'])
        self.denied('POST', '/tasks', dict(title='Bad upload', subjectId=self.subject, attachment=invalid))
        self.assertEqual(1, len(self.call('GET', '/state')['tasks']))
        too_large = dict(name='large.pdf', content=base64.b64encode(b'%PDF-' + b'x' * (10 * 1024 * 1024)).decode())
        self.denied('POST', f'/tasks/{tid}/attachments', too_large)
        self.call('DELETE', f'/tasks/{tid}')
        self.denied('GET', f'/attachments/{aid}', status=404)

    def test_http_boundary_and_rate_limit(self):
        app = Application(self.store)
        def request(path, body=b'{}', token='', method='POST', length=None):
            response = []
            env = {'REQUEST_METHOD': method, 'PATH_INFO': path, 'CONTENT_LENGTH': str(len(body)) if length is None else length,
                   'wsgi.input': io.BytesIO(body), 'HTTP_AUTHORIZATION': 'Bearer ' + token, 'REMOTE_ADDR': '127.0.0.1'}
            result = b''.join(app(env, lambda status, headers: response.append((status, headers))))
            return response[0][0], json.loads(result)
        self.assertTrue(request('/state', token=self.owner, method='GET')[0].startswith('200'))
        self.assertTrue(request('/state', method='GET')[0].startswith('401'))
        self.assertTrue(request('/subjects', body=b'{bad', token=self.owner)[0].startswith('400'))
        self.assertTrue(request('/state', body=b'[]', token=self.owner, method='GET')[0].startswith('400'))
        self.assertTrue(request('/state', token=self.owner, method='GET', length='invalid')[0].startswith('400'))
        for _ in range(15):
            request('/auth/login')
        self.assertTrue(request('/auth/login')[0].startswith('429'))

    def test_malformed_task_payloads_are_rejected_without_partial_writes(self):
        before = self.call('GET', '/state')['tasks']
        for invalid in (["not", "an", "object"], "text", 7):
            with self.subTest(attachment=invalid):
                self.denied('POST', '/tasks', dict(title='Invalid', subjectId=self.subject, attachment=invalid), status=400)
            with self.subTest(groupId=invalid):
                self.denied('POST', '/tasks', dict(title='Invalid', subjectId=self.subject, groupId=invalid), status=403 if isinstance(invalid, str) else 400)
        self.assertEqual(before, self.call('GET', '/state')['tasks'])

    def test_meeting_delete_permissions(self):
        log = self.call('POST', f'/groups/{self.group}/logs', dict(date='2030-10-01',
            attendees='Owner', decisions='Review', actionItems='Test'))['id']
        self.denied('DELETE', f'/logs/{log}', token=self.member, status=403)
        self.assertEqual(log, self.call('GET', '/state')['logs'][0]['id'])
        self.call('DELETE', f'/logs/{log}')
        self.assertEqual([], self.call('GET', '/state')['logs'])


class HttpRoundTripTests(unittest.TestCase):
    def test_socket_login_save_restart_and_reload(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / 'roundtrip.sqlite3'
            def run_server():
                server = make_server('127.0.0.1', 0, Application(Store(path)), handler_class=QuietHandler)
                thread = threading.Thread(target=server.serve_forever, daemon=True)
                thread.start()
                return server, thread
            server, thread = run_server()
            def request(method, path, data=None, token=''):
                payload = json.dumps(data).encode() if data is not None else None
                req = urllib.request.Request(f'http://127.0.0.1:{server.server_port}{path}', data=payload,
                    headers={'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token}, method=method)
                with urllib.request.urlopen(req, timeout=10) as response:
                    return json.load(response)
            try:
                self.assertTrue(request('GET', '/health')['ok'])
                account = dict(name='HTTP User', email='http@example.com', password='RoundTrip123!', confirmPassword='RoundTrip123!')
                request('POST', '/auth/register', account)
                token = request('POST', '/auth/login', account)['token']
                subject = request('POST', '/subjects', {'name': 'Fisika'}, token)['id']
                task = request('POST', '/tasks', dict(title='Uji koneksi', subjectId=subject, deadline='2030-10-01'), token)['id']
            finally:
                server.shutdown(); thread.join(); server.server_close()
            server, thread = run_server()
            try:
                state = request('GET', '/state', token=token)
                self.assertEqual(task, state['tasks'][0]['id'])
                request('PUT', f'/tasks/{task}', {'status': 'Done'}, token)
                self.assertEqual('Done', request('GET', '/state', token=token)['tasks'][0]['status'])
                request('POST', '/auth/logout', {}, token)
                with self.assertRaises(urllib.error.HTTPError) as caught:
                    request('GET', '/state', token=token)
                self.assertEqual(401, caught.exception.code)
            finally:
                server.shutdown(); thread.join(); server.server_close()


if __name__ == '__main__':
    unittest.main()
