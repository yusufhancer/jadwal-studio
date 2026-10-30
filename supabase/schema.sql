-- ========================================================
-- JADWALSTUDIO - SUPABASE PRODUCTION DATABASE SCHEMA
-- ========================================================

-- 1. EXTENSIONS
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. PROFILES TABLE (Linked with Supabase Auth)
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    photo_url TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Trigger untuk membuat profile otomatis saat user register di Auth Supabase
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, name, email, photo_url)
    VALUES (
        NEW.id,
        COALESCE(NEW.raw_user_meta_data->>'name', split_part(NEW.email, '@', 1)),
        NEW.email,
        NULL
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- 3. SUBJECTS TABLE (Mata Pelajaran)
CREATE TABLE IF NOT EXISTS public.subjects (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, name)
);

-- 4. GROUPS TABLE (Kelompok Belajar)
CREATE TABLE IF NOT EXISTS public.groups (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    description TEXT DEFAULT '',
    owner_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    invite_code TEXT NOT NULL UNIQUE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. GROUP MEMBERS TABLE
CREATE TABLE IF NOT EXISTS public.group_members (
    group_id UUID NOT NULL REFERENCES public.groups(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    role TEXT NOT NULL DEFAULT 'member', -- 'owner' or 'member'
    joined_at TIMESTAMPTZ DEFAULT NOW(),
    PRIMARY KEY (group_id, user_id)
);

-- Trigger untuk menambahkan owner otomatis sebagai member saat grup dibuat
CREATE OR REPLACE FUNCTION public.handle_new_group_owner()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.group_members (group_id, user_id, role)
    VALUES (NEW.id, NEW.owner_id, 'owner')
    ON CONFLICT DO NOTHING;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_group_created ON public.groups;
CREATE TRIGGER on_group_created
    AFTER INSERT ON public.groups
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_group_owner();

-- 6. TASKS TABLE (Tugas Individu & Kelompok)
CREATE TABLE IF NOT EXISTS public.tasks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    subject_id UUID NOT NULL REFERENCES public.subjects(id) ON DELETE CASCADE,
    group_id UUID REFERENCES public.groups(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    description TEXT DEFAULT '',
    notes TEXT DEFAULT '',
    deadline TEXT NOT NULL, -- Format: YYYY-MM-DD
    priority INT NOT NULL DEFAULT 0, -- 1 = Mendesak/Penting, 0 = Normal
    status TEXT NOT NULL DEFAULT 'To Do', -- 'To Do', 'In Progress', 'Review', 'Done'
    created_by UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 7. TASK ASSIGNEES / MEMBERS
CREATE TABLE IF NOT EXISTS public.task_members (
    task_id UUID NOT NULL REFERENCES public.tasks(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    PRIMARY KEY (task_id, user_id)
);

-- 8. SCHEDULES TABLE (Jadwal Pelajaran)
CREATE TABLE IF NOT EXISTS public.schedules (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    subject_id UUID NOT NULL REFERENCES public.subjects(id) ON DELETE CASCADE,
    day TEXT NOT NULL, -- 'Senin', 'Selasa', 'Rabu', 'Kamis', 'Jumat'
    start_time TEXT NOT NULL, -- '07:00'
    end_time TEXT NOT NULL, -- '08:30'
    room TEXT DEFAULT '',
    teacher TEXT DEFAULT '',
    note TEXT DEFAULT '',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 9. GROUP MEETING LOGS (Catatan Rapat / Notulensi)
CREATE TABLE IF NOT EXISTS public.group_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    group_id UUID NOT NULL REFERENCES public.groups(id) ON DELETE CASCADE,
    created_by UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    date TEXT NOT NULL,
    attendees TEXT NOT NULL,
    decisions TEXT NOT NULL,
    action_items TEXT NOT NULL,
    next_meeting_date TEXT DEFAULT '',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 10. ATTACHMENTS TABLE
CREATE TABLE IF NOT EXISTS public.attachments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    task_id UUID NOT NULL REFERENCES public.tasks(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    mime TEXT NOT NULL,
    size INT NOT NULL,
    storage_path TEXT NOT NULL,
    uploaded_by UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ========================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ========================================================

ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.subjects ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.groups ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.group_members ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.tasks ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.task_members ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.schedules ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.group_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.attachments ENABLE ROW LEVEL SECURITY;

-- Profiles: Semua user login bisa melihat profil untuk kolaborasi grup, edit hanya miliknya
CREATE POLICY "Profiles readable by authenticated" ON public.profiles FOR SELECT TO authenticated USING (true);
CREATE POLICY "Profiles updatable by owner" ON public.profiles FOR UPDATE TO authenticated USING (auth.uid() = id);

-- Subjects: User hanya bisa akses mapel miliknya sendiri
CREATE POLICY "Subjects isolation" ON public.subjects FOR ALL TO authenticated USING (auth.uid() = user_id);

-- Groups: Hanya member atau owner yang bisa lihat/akses
CREATE POLICY "Groups select member" ON public.groups FOR SELECT TO authenticated
    USING (id IN (SELECT group_id FROM public.group_members WHERE user_id = auth.uid()));
CREATE POLICY "Groups insert owner" ON public.groups FOR INSERT TO authenticated
    WITH CHECK (auth.uid() = owner_id);
CREATE POLICY "Groups update owner" ON public.groups FOR UPDATE TO authenticated
    USING (auth.uid() = owner_id);
CREATE POLICY "Groups delete owner" ON public.groups FOR DELETE TO authenticated
    USING (auth.uid() = owner_id);

-- Group Members
CREATE POLICY "Members view in same group" ON public.group_members FOR SELECT TO authenticated
    USING (group_id IN (SELECT group_id FROM public.group_members gm WHERE gm.user_id = auth.uid()));
CREATE POLICY "Members join group" ON public.group_members FOR INSERT TO authenticated
    WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Members remove" ON public.group_members FOR DELETE TO authenticated
    USING (auth.uid() = user_id OR auth.uid() IN (SELECT owner_id FROM public.groups g WHERE g.id = group_id));

-- Tasks: Akses tugas individu (milik sendiri) ATAU tugas kelompok (user terdaftar sebagai member grup tersebut)
CREATE POLICY "Tasks access" ON public.tasks FOR ALL TO authenticated
    USING (
        (group_id IS NULL AND created_by = auth.uid()) OR
        (group_id IS NOT NULL AND group_id IN (SELECT group_id FROM public.group_members WHERE user_id = auth.uid()))
    );

-- Schedules
CREATE POLICY "Schedules isolation" ON public.schedules FOR ALL TO authenticated USING (auth.uid() = user_id);

-- Group Logs
CREATE POLICY "Logs group member" ON public.group_logs FOR ALL TO authenticated
    USING (group_id IN (SELECT group_id FROM public.group_members WHERE user_id = auth.uid()));

-- ========================================================
-- AGGREGATED STATE RPC (Untuk Sinkronisasi Sekali Panggil /state)
-- ========================================================
CREATE OR REPLACE FUNCTION public.get_app_state()
RETURNS JSON AS $$
DECLARE
    current_uid UUID := auth.uid();
    result JSON;
BEGIN
    SELECT json_build_object(
        'user', (
            SELECT json_build_object(
                'id', p.id,
                'name', p.name,
                'email', p.email,
                'photo', p.photo_url
            )
            FROM public.profiles p
            WHERE p.id = current_uid
        ),
        'subjects', COALESCE((
            SELECT json_agg(json_build_object('id', s.id, 'name', s.name))
            FROM public.subjects s
            WHERE s.user_id = current_uid
        ), '[]'::json),
        'groups', COALESCE((
            SELECT json_agg(json_build_object(
                'id', g.id,
                'name', g.name,
                'description', g.description,
                'owner_id', g.owner_id,
                'invite_code', g.invite_code,
                'members', (
                    SELECT COALESCE(json_agg(json_build_object(
                        'id', mp.id,
                        'name', mp.name,
                        'email', mp.email,
                        'photo', mp.photo_url,
                        'role', gm.role
                    )), '[]'::json)
                    FROM public.group_members gm
                    JOIN public.profiles mp ON mp.id = gm.user_id
                    WHERE gm.group_id = g.id
                )
            ))
            FROM public.groups g
            WHERE g.id IN (SELECT group_id FROM public.group_members WHERE user_id = current_uid)
        ), '[]'::json),
        'tasks', COALESCE((
            SELECT json_agg(json_build_object(
                'id', t.id,
                'title', t.title,
                'subject', sub.name,
                'subject_id', t.subject_id,
                'group_id', t.group_id,
                'priority', t.priority,
                'status', t.status,
                'deadline', t.deadline,
                'description', t.description,
                'notes', t.notes
            ))
            FROM public.tasks t
            JOIN public.subjects sub ON sub.id = t.subject_id
            WHERE (t.group_id IS NULL AND t.created_by = current_uid)
               OR (t.group_id IS NOT NULL AND t.group_id IN (SELECT group_id FROM public.group_members WHERE user_id = current_uid))
        ), '[]'::json),
        'schedules', COALESCE((
            SELECT json_agg(json_build_object(
                'id', sc.id,
                'subject', sub.name,
                'day', sc.day,
                'start_time', sc.start_time,
                'end_time', sc.end_time,
                'room', sc.room,
                'teacher', sc.teacher,
                'note', sc.note
            ))
            FROM public.schedules sc
            JOIN public.subjects sub ON sub.id = sc.subject_id
            WHERE sc.user_id = current_uid
        ), '[]'::json)
    ) INTO result;

    RETURN result;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ========================================================
-- 11. RPC: JOIN GROUP BY INVITE CODE
-- ========================================================
CREATE OR REPLACE FUNCTION public.join_group_by_code(token TEXT)
RETURNS JSON AS $$
DECLARE
    target_group RECORD;
    current_uid UUID := auth.uid();
BEGIN
    SELECT * INTO target_group FROM public.groups WHERE invite_code = token LIMIT 1;
    IF target_group IS NULL THEN
        RAISE EXCEPTION 'Kode undangan tidak valid atau kelompok tidak ditemukan.';
    END IF;

    INSERT INTO public.group_members (group_id, user_id, role)
    VALUES (target_group.id, current_uid, 'member')
    ON CONFLICT (group_id, user_id) DO NOTHING;

    RETURN json_build_object('id', target_group.id, 'name', target_group.name);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ========================================================
-- 12. APP VERSIONS TABLE (In-App Auto Update)
-- ========================================================
CREATE TABLE IF NOT EXISTS public.app_versions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    version_code INT NOT NULL UNIQUE,
    version_name TEXT NOT NULL,
    apk_url TEXT NOT NULL,
    changelog TEXT DEFAULT '',
    is_mandatory BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.app_versions ENABLE ROW LEVEL SECURITY;
CREATE POLICY "App versions readable by everyone" ON public.app_versions FOR SELECT USING (true);

