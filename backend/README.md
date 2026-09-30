# Backend lokal JadwalStudio

Backend ini menyimpan akun, sesi, mata pelajaran, tugas, jadwal, kelompok, anggota,
catatan meeting, serta isi lampiran di SQLite. Tidak perlu layanan cloud untuk
pengujian lokal. Database dibuat otomatis di `backend/data/jadwalstudio.sqlite3`.
Tidak ada akun atau password bawaan; daftar lewat aplikasi.

Foto profil disimpan per akun di SQLite. Pilih foto melalui avatar > Ganti foto
profil; aplikasi mengecilkan foto sebelum mengunggah. Setelah memperbarui kode
backend, hentikan server dengan Ctrl+C lalu jalankan kembali start-local.ps1.
Tabel foto ditambahkan otomatis tanpa menghapus data yang sudah ada.

## Menjalankan

Dari folder project, jalankan PowerShell:

```powershell
.\backend\start-local.ps1
```

Biarkan terminal ini berjalan. Cek `http://127.0.0.1:8765/health`.
Runtime Python yang disiapkan untuk workspace ini ada di `.tools/python`.
Backend hanya memakai pustaka standar Python 3.12.

### HP melalui USB

Aktifkan USB debugging, sambungkan HP, lalu:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" reverse tcp:8765 tcp:8765
```

Pasang APK debug. Pada halaman login, buka **Koneksi server lokal** dan gunakan
`http://127.0.0.1:8765`. Daftar akun, lalu masuk. Ulangi perintah `adb reverse`
setelah kabel dicabut/ADB direstart. Beberapa HP lewat USB dapat memakai server
yang sama, masing-masing dengan akun sendiri.

### Emulator

Gunakan `http://10.0.2.2:8765` pada pengaturan server aplikasi.

### Beberapa HP pada Wi-Fi yang sama

Jalankan `./backend/start-local.ps1 -HostAddress 0.0.0.0`. Gunakan
`http://IP-KOMPUTER:8765` di setiap HP; Windows Firewall perlu mengizinkan port
8765 pada jaringan privat. Jangan pakai `127.0.0.1` untuk koneksi Wi-Fi antar
perangkat. Gunakan akun dan data uji pada jaringan lokal tepercaya: HTTP debug
tidak mengenkripsi lalu lintas. Link `jadwalstudio://join/TOKEN` membuka aplikasi
dan mengisi kode pada menu Gabung Kelompok; pengguna mengonfirmasi dengan tombol
Gabung. Jika aplikasi berbagi pesan tidak membuat link ini dapat diklik, salin
dan tempel link/kodenya. Kedua HP harus memakai alamat backend yang sama.
Endpoint HTTP `/join/TOKEN` juga menerima pembukaan lewat browser dan menampilkan
petunjuk bergabung.

## Alur uji manual

1. Daftar dua akun berbeda. Email duplikat dan konfirmasi password salah ditolak.
2. Akun A: tambah mata pelajaran, tugas individu, deskripsi, catatan, dan lampiran.
3. Tutup/buka aplikasi dan restart backend. Data tetap tersedia.
4. Tambah jadwal Senin 08:00–09:00. Jadwal 08:30–09:30 ditolak; 09:00–10:00 boleh.
5. Buat kelompok, bagikan link, masuk sebagai B dan bergabung. Percobaan join
   ulang ditolak; setelah link diganti owner, link lama tidak berlaku.
6. Buat tugas kelompok. Buka detail tugas dan pilih satu/beberapa penerima.
   Ubah status ke In Progress, Review, Done; lihat kontribusi di halaman anggota.
7. Akun yang bukan anggota tidak dapat membaca/mengubah data kelompok. Member
   tidak dapat mengganti undangan/menghapus anggota. Pembuat, owner, dan penerima
   tugas boleh mengubah tugas sesuai aturan PRD.
8. Buat catatan meeting. Tanggal, peserta, keputusan, dan tindak lanjut wajib diisi.
9. Coba lampiran PDF/JPG/PNG/DOCX yang valid, format palsu, dan file >10 MB.
10. Logout, lalu login lagi. Token lama ditolak oleh server; cache sesi dibersihkan.

Buka profil lewat foto/inisial di header, lalu tekan **Muat Ulang** untuk mengambil perubahan dari HP lain. Cache perangkat
bersifat baca-saja saat offline; penyimpanan/perubahan membutuhkan backend aktif.
Centang Ingat Saya untuk mempertahankan login setelah proses aplikasi dihentikan.

## Pengujian otomatis

```powershell
.\.tools\python\cpython-3.12.14-windows-x86_64-none\python.exe -m unittest discover -s backend -v
```

Tes menggunakan database sementara terpisah dan tidak menyentuh data aplikasi.
Tes Android JVM dijalankan dengan `gradlew.bat :app:testDebugUnitTest`.
Jalankan `./build-local.ps1` dari root project untuk build APK, tes JVM, dan lint
dengan JDK 17 lokal. Di Android Studio, pilih JDK 17 sebagai Gradle JDK; JDK 25
bawaan instalasi Studio pada komputer ini membuat versi lint project crash.

## Data dan keamanan

- Password menggunakan PBKDF2-HMAC-SHA256 dengan salt acak, 600.000 iterasi.
- Token sesi acak disimpan sebagai hash di server; logout menghapus sesi.
- Database memisahkan data personal per user dan data kelompok per membership.
- Validasi dan perubahan berlangsung dalam transaksi, termasuk bentrok jadwal
  dan upload bersamaan dengan pembuatan/perubahan tugas.
- Lampiran disimpan sebagai BLOB, dibatasi 10 MB, diperiksa ekstensi dan signature.
- Cache/token Android dienkripsi AES-GCM dengan kunci Android Keystore dan backup
  aplikasi dinonaktifkan. Password tidak disimpan di perangkat.
- Backup database dilakukan ketika backend berhenti, atau gunakan SQLite backup
  API agar berkas WAL tidak terlewat. Jangan menghapus folder `backend/data` jika
  ingin mempertahankan akun dan tugas.

## Batas deployment lokal

Runner WSGI bawaan disediakan untuk pengembangan lokal, bukan server publik.
Hosting publik belum disiapkan. Untuk produksi, jalankan objek
`Application(Store(path_database))` dengan server WSGI produksi, HTTPS, pemantauan,
backup, dan pengaturan domain. Tetapkan URL HTTPS lewat Gradle
`-PstudioApiUrl=https://domain-api-anda` saat build. Build release menolak HTTP.

Reset password otomatis, transfer ownership, dan penghapusan akun/kelompok tidak
diaktifkan; fitur tersebut berada di luar MVP PRD. Owner tidak bisa menghapus
dirinya lewat menu anggota. Tidak ada leaderboard.

Referensi implementasi:
- https://docs.python.org/3/library/http.server.html (batas runner development)
- https://developer.android.com/privacy-and-security/security-config
- https://developer.android.com/privacy-and-security/keystore
