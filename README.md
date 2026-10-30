# JadwalStudio - Android Study & Task Planner

Aplikasi mobile Android modern untuk manajemen jadwal pelajaran, tugas sekolah/kuliah (individu & kelompok), serta catatan notulensi rapat kelompok. Dibangun menggunakan **Jetpack Compose** dan **Material 3** dengan arsitektur reaktif yang terintegrasi penuh ke **Supabase Cloud**.

---

## Fitur Utama

- **Autentikasi Aman**: Registrasi, login, dan penyimpanan token sesi terenkripsi menggunakan **AndroidKeyStore (AES-256-GCM)**.
- **Dashboard Ringkasan**: Menampilkan jadwal pelajaran hari ini, tugas yang mendekati deadline, serta daftar kelompok belajar aktif.
- **Tugas Individu & Kelompok**: Pengelompokan tugas berdasarkan mata pelajaran, deadline, prioritas mendesak, status pengerjaan (*To Do, In Progress, Done*), dan lampiran dokumen/gambar.
- **Kolaborasi Kelompok**: Fitur buat kelompok, undang anggota via *deep link* / kode undangan (`jadwalstudio://join/<token>`), daftar anggota, dan notulensi rapat kelompok.
- **Jadwal Pelajaran**: Pengaturan jadwal mingguan (Senin–Jumat) lengkap dengan jam, ruangan, nama pengajar, dan catatan.
- **In-App Auto Update**: Notifikasi pop-up otomatis di dalam aplikasi saat pengembang merilis versi APK baru tanpa mengharuskan pengguna uninstall manual.
- **Offline Resilient**: Sinkronisasi data satu pintu via RPC Supabase `get_app_state()` dengan penyimpanan cache lokal terenkripsi (*AtomicFile*).

---

## Teknologi & Arsitektur

| Komponen | Teknologi |
|---|---|
| **Bahasa** | Kotlin 2.0 |
| **UI Framework** | Jetpack Compose (BOM 2024.06.00) + Material 3 |
| **Asynchronous** | Kotlin Coroutines & Dispatchers |
| **Backend & Database** | Supabase Cloud (PostgreSQL 15+, GoTrue Auth, PostgREST) |
| **Penyimpanan File** | Supabase Storage (Bucket: `attachments`, `avatars`, `releases`) |
| **Keamanan Lokal** | AndroidKeyStore AES/GCM Hardware-backed Encryption |
| **Minimum Android SDK** | Android 7.0 (API Level 24) |
| **Target SDK** | Android 14 (API Level 34) |

---

## Struktur Folder Proyek

```
apklasi-jadwalstudio/
├── app/
│   ├── build.gradle.kts                # Konfigurasi Gradle module app & credentials Supabase
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/jadwalstudio/app/
│       │   │   ├── MainActivity.kt     # Entry point & root navigation state
│       │   │   ├── NavigationRules.kt  # Logika back stack navigation
│       │   │   ├── data/
│       │   │   │   ├── StudioRepository.kt  # Supabase REST client, session, update checker
│       │   │   │   └── AttachmentInput.kt
│       │   │   ├── ui/
│       │   │   │   ├── components/     # AppHeader, AppBottomBar, AppUpdateDialog, dll.
│       │   │   │   ├── screens/        # Dashboard, Task, Schedule, Group, Auth screens
│       │   │   │   └── theme/          # Color (SageGreen/BgCream), Typography, Theme
│       │   │   └── utils/              # TaskDateUtils, PhotoSizing, DashboardData
│       │   └── res/                    # Drawables, strings, XML resources
│       └── test/                       # Unit tests
├── supabase/
│   ├── schema.sql                      # DDL PostgreSQL lengkap, RLS, Triggers, RPCs
│   └── README.md                       # Dokumentasi setup backend Supabase
├── build.gradle.kts                    # Root build configuration
├── gradle.properties                   # JVM & AndroidX properties
├── settings.gradle.kts
└── README.md
```

---

## Panduan Pengembang (Development Setup)

### 1. Prasyarat
- **Android Studio**: Versi Koala (2024.1+) atau yang lebih baru.
- **JDK**: Java 17 atau Java 21 (sudah include di Android Studio).
- **Android SDK**: Platform SDK API 34.

### 2. Cara Menjalankan Proyek
1. Clone repositori ini:
   ```bash
   git clone https://github.com/yusufhancer/jadwal-studio.git
   ```
2. Buka Android Studio, pilih **Open**, lalu arahkan ke folder `jadwal-studio`.
3. Tunggu proses **Gradle Sync** selesai secara otomatis.
4. Hubungkan HP Android fisik (aktifkan *USB Debugging*) atau jalankan Android Virtual Device (AVD).
5. Klik tombol **Run 'app'** (`Shift + F10`) di Android Studio.

---

## Konfigurasi Backend Supabase

Konfigurasi Supabase bawaan sudah tertanam di [`app/build.gradle.kts`](app/build.gradle.kts):

```kotlin
buildConfigField("String", "SUPABASE_URL", "\"https://ppdsgwcdbdhplokzzkxu.supabase.co\"")
buildConfigField("String", "SUPABASE_KEY", "\"sb_publishable_VWQYA3ijvPGTv2HOnpWUbA_vbOQdWbi\"")
buildConfigField("String", "API_BASE_URL", "\"${providers.gradleProperty("studioApiUrl").getOrElse("https://ppdsgwcdbdhplokzzkxu.supabase.co")}\"")
```

Jika Anda ingin menggunakan instance Supabase Anda sendiri:
1. Buka [Supabase](https://supabase.com) dan buat proyek baru.
2. Buka menu **SQL Editor**, salin seluruh isi skrip dari file [`supabase/schema.sql`](supabase/schema.sql), lalu jalankan (**Run**).
3. Buat storage bucket di menu **Storage**:
   - `attachments` (Maksimal 10MB)
   - `avatars` (Publik, Maksimal 5MB)
   - `releases` (Publik, Maksimal 100MB)
4. Perbarui `SUPABASE_URL` dan `SUPABASE_KEY` di `app/build.gradle.kts`.

---

## Alur Rilis Pembaruan APK (In-App Auto Update)

Agar pengguna aplikasi Anda langsung menerima notifikasi update:
1. Di `app/build.gradle.kts`, naikkan versinya:
   ```kotlin
   versionCode = 2
   versionName = "1.0.1"
   ```
2. Build file APK di Android Studio: **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
3. Unggah file APK ke Supabase Storage di bucket `releases`.
4. Masukkan baris baru di tabel `app_versions`:
   ```sql
   INSERT INTO public.app_versions (version_code, version_name, apk_url, changelog, is_mandatory)
   VALUES (
       2,
       '1.0.1',
       'https://ppdsgwcdbdhplokzzkxu.supabase.co/storage/v1/object/public/releases/app-release.apk',
       '- Tampilan baru yang lebih segar\n- Peningkatan kecepatan sinkronisasi',
       false
   );
   ```
5. Ketika user membuka aplikasi, dialog pop-up pembaruan akan langsung tampil dan pengguna cukup menekan **Perbarui Sekarang**.

---

## Kontribusi & Lisensi

Dibuat untuk memudahkan pelajar dan mahasiswa dalam mengorganisasi jadwal dan tugas sekolah/kuliah secara mandiri maupun kolaboratif. Silakan submit *Pull Request* atau buat *Issue* untuk pengembangan lebih lanjut!
