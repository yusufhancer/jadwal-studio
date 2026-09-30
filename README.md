# JadwalStudio

Aplikasi Android untuk manajemen jadwal pelajaran, tugas individu, tugas kelompok, dan catatan meeting berbasis Jetpack Compose dengan backend Python SQLite.

Repository ini disiapkan agar teman satu tim bisa langsung clone, menjalankan server pengujian, dan lanjut mengembangkan aplikasinya di Android Studio.

---

## 1. Cara Download / Clone Project

### Pilihan A: Pakai Git (Direkomendasikan)
Buka terminal atau Git Bash, lalu jalankan:

```bash
git clone https://github.com/yusufhancer/jadwal-studio.git
```

### Pilihan B: Tanpa Git
Buka halaman repository di browser, klik tombol hijau bertuliskan **Code**, lalu pilih **Download ZIP**. Setelah itu ekstrak foldernya di laptop kamu.

---

## 2. Cara Menjalankan Server Backend

Aplikasi ini butuh server backend untuk fitur login, registrasi, simpan tugas, dan sinkronisasi data kelompok. Servernya sudah disediakan di dalam repo ini.

### Cara Paling Praktis (Bisa Buat HP Tanpa Kabel USB)
1. Di dalam folder project, cari file `start-online.bat`.
2. Dobel klik file `start-online.bat`.
3. Script akan otomatis mendownload Cloudflare tunnel jika belum ada, menjalankan server lokal, dan membuat link publik HTTPS.
4. Tunggu beberapa detik sampai muncul tulisan:
   `URL Publik Server Anda: >>> https://random-name.trycloudflare.com <<<`
5. Biarkan jendela terminal tersebut tetap terbuka selama kamu ngetes aplikasi.

Kalau terminalnya tiba-tiba bengong beberapa detik pas nyambungin ke edge, jangan langsung panik teriak 6-7 sambil ngayun tangan ke atas bawah kayak bocah AAU. Itu lagi nunggu jalur tunnel doang, santai dulu.

### Cara Lokal Biasa (Lewat USB)
Kalau kamu mau pakai koneksi lokal tanpa internet:
1. Buka PowerShell di folder project.
2. Jalankan perintah:
   ```powershell
   .\backend\start-local.ps1
   ```
3. Sambungkan HP lewat kabel USB (pastikan USB Debugging aktif), lalu jalankan perintah ADB reverse:
   ```powershell
   adb reverse tcp:8765 tcp:8765
   ```

---

## 3. Cara Buka dan Jalankan Aplikasi di Android Studio

1. Buka **Android Studio**.
2. Pilih menu **File** -> **Open...**, lalu arahkan ke folder project `jadwal-studio`.
3. Tunggu Android Studio menyelesaikan proses sinkronisasi Gradle (biasanya perlu waktu sebentar saat pertama kali dibuka).
4. Sambungkan HP Android kamu dengan kabel data atau siapkan Emulator.
5. Klik tombol **Run** (segitiga hijau) di bagian toolbar atas, atau tekan tombol **Shift + F10**.
6. Aplikasi akan terpasang dan otomatis terbuka di HP kamu.

---

## 4. Cara Menghubungkan Aplikasi ke Server

Saat aplikasi baru pertama kali dibuka:
1. Di layar awal login, klik tombol **Koneksi server lokal**.
2. Masukkan alamat server yang tadi kamu dapatkan:
   - Jika pakai `start-online.bat`, masukkan link HTTPS lengkapnya, contoh: `https://contoh-nama.trycloudflare.com`
   - Jika pakai kabel USB dan adb reverse, masukkan: `http://127.0.0.1:8765`
3. Klik tombol **Simpan**.
4. Sekarang kamu bisa klik menu **Daftar Akun**, buat akun baru, dan mulai coba semua fiturnya.

---

## 5. Mau Kirim Perubahan Kode (Push ke GitHub)?

Setelah kamu selesai ngoding fitur baru:
1. Pastikan kodingan kamu berjalan lancar tanpa error build.
2. Buka terminal di folder project, lalu jalankan:
   ```bash
   git add .
   git commit -m "penjelasan singkat fitur yang kamu tambah atau perbaiki"
   git push
   ```
