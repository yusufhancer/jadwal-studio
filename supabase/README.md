# Panduan Backend Supabase JadwalStudio

Backend JadwalStudio menggunakan **Supabase Cloud** (PostgreSQL, GoTrue Auth, Storage, dan PostgREST).

---

## 1. Skema Database

Seluruh tabel, trigger relasi, Row Level Security (RLS), dan Stored Procedure (RPC) telah disediakan dalam file [`schema.sql`](schema.sql):

- **`profiles`**: Terhubung otomatis dengan Supabase Auth (`auth.users`) melalui trigger `handle_new_user`.
- **`subjects`**: Data mata pelajaran privat per pengguna.
- **`groups` & `group_members`**: Manajemen kelompok belajar dan anggota.
- **`tasks` & `task_members`**: Tugas individu dan kelompok dengan status, deadline, dan prioritas.
- **`schedules`**: Jadwal pelajaran harian.
- **`group_logs`**: Catatan/notulensi rapat kelompok.
- **`attachments`**: Metadata lampiran file tugas (PDF, JPG, PNG, DOCX).
- **`app_versions`**: Manajemen versi APK untuk in-app auto-update checker.
- **RPC `get_app_state()`**: Sinkronisasi seluruh state aplikasi dalam 1 pemanggilan jaringan terenkripsi.
- **RPC `join_group_by_code(token)`**: Bergabung ke kelompok menggunakan tautan/token undangan.

---

## 2. Storage Buckets

Pastikan ketiga bucket berikut dibuat di menu **Storage** Supabase:

| Bucket | Akses Publik | Batasan Ukuran | Tipe File |
|---|---|---|---|
| `attachments` | Private / Authenticated | 10 MB | pdf, docx, jpg, png |
| `avatars` | Public | 5 MB | jpg, png |
| `releases` | Public | 100 MB | apk |

---

## 3. Konfigurasi Client Android

Di file `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "SUPABASE_URL", "\"https://<project-ref>.supabase.co\"")
buildConfigField("String", "SUPABASE_KEY", "\"<anon_or_publishable_key>\"")
buildConfigField("String", "API_BASE_URL", "\"${providers.gradleProperty("studioApiUrl").getOrElse("https://<project-ref>.supabase.co")}\"")
```

---

## 4. Alur Distribusi Update APK Baru

1. Naikkan `versionCode` dan `versionName` di `app/build.gradle.kts`.
2. Generate APK di Android Studio: **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
3. Unggah file APK ke Supabase Storage pada bucket `releases` (publik).
4. Tambahkan catatan versi baru di tabel `app_versions`:
   ```sql
   INSERT INTO public.app_versions (version_code, version_name, apk_url, changelog, is_mandatory)
   VALUES (
       2,
       '1.0.1',
       'https://<project-ref>.supabase.co/storage/v1/object/public/releases/app-release.apk',
       '- Pembaruan stabilitas dan perbaikan tampilan',
       false
   );
   ```
Aplikasi pengguna akan otomatis memunculkan dialog pembaruan saat dibuka!
