# Audit debug — 28 September 2026

## Perbaikan putaran ini

- Refresh repository diserialkan agar respons refresh lama tidak menimpa respons yang lebih baru.
- Hapus catatan rapat hanya ditampilkan untuk pembuat/owner; perlu konfirmasi sebelum menghapus.
- Dialog tambah mata pelajaran dan gabung kelompok tetap terbuka saat permintaan gagal; isian dipertahankan saat rekreasi layar.
- API memvalidasi tipe groupId, objek lampiran, body JSON, dan Content-Length. Input rusak ditolak tanpa menyimpan tugas sebagian.
- Perapian blok UI yang dilaporkan lint, tanpa redesain.

## Pemeriksaan

- Tes regresi API input rusak direproduksi gagal sebelum perbaikan, lalu lolos setelahnya.
- Suite backend: 17 tes lolos, termasuk hak hapus rapat dan transaksi lampiran gagal.
- Tes HTTP boundary diulang setelah menambah kasus body array dan Content-Length tidak valid: lolos.
- Build Android, unit test, dan lint dijalankan terpisah; lihat laporan terbaru di app/build/reports.
- ADB tidak mendeteksi perangkat pada audit ini. Ini bukan sertifikasi seluruh aplikasi bebas bug.

## Verifikasi perangkat yang masih diperlukan

1. Matikan backend, coba tambah mata pelajaran/gabung kelompok; pastikan dialog dan isian tetap ada setelah error. Hidupkan backend dan coba lagi.
2. Login sebagai pembuat, owner, dan anggota lain; cek tombol hapus catatan hanya muncul untuk yang berhak. Batal tidak menghapus data.
3. Coba rotasi saat mengisi form, memilih lampiran, dan saat permintaan sedang berlangsung.
4. Uji dua HP: perubahan anggota, pencabutan akses, refresh, sesi kedaluwarsa, dan foto profil.
5. Uji putus koneksi saat unggah/simpan. Respons yang hilang setelah server sudah menyimpan masih perlu diuji untuk risiko pengiriman ulang.

Restart backend lokal untuk memuat perbaikan server; pasang APK melalui Run Android Studio tanpa menghapus data aplikasi.
