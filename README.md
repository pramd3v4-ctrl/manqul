# Manqul (Android native, Kotlin + Jetpack Compose)

## Cara mendapatkan APK
### A. Tanpa Android Studio (GitHub Actions)
1. Buat repositori baru di github.com, unggah seluruh isi folder ini (termasuk folder .github).
2. Buka tab Actions, jalankan "Build APK" (Run workflow), tunggu sekitar 5 menit.
3. Unduh artefak "Manqul-APK", ekstrak, lalu pasang app-debug.apk di HP (izinkan "instal dari sumber tidak dikenal").

### B. Android Studio
Open folder ini, tunggu sinkronisasi Gradle, lalu Build > Build Bundle(s) / APK(s) > Build APK(s).

## Penyimpanan data
Arti, keterangan, dan teks Al-Quran disimpan di penyimpanan privat aplikasi (filesDir).
allowBackup aktif, jadi Android dapat mencadangkannya ke akun Google. Data hilang bila aplikasi dihapus tanpa cadangan.

## Teks Al-Quran
Bawaan hanya Al-Fatihah. Buka Pengaturan > Buka tanzil.net/download, unduh format "Text (with aya numbers)", lalu pilih file itu di aplikasi.

## Catatan
APK ini berjenis debug (cukup untuk dipakai pribadi). Untuk Play Store perlu build release yang ditandatangani.
Kode belum pernah dikompilasi di lingkungan pembuatnya; bila build gagal, kirim log kesalahannya.

## Kunci penandatangan tetap
app/manqul.keystore dipakai untuk menandatangani APK di setiap build, sehingga versi baru bisa dipasang menimpa versi lama tanpa menghapus data.
Kata sandi kunci tertulis di app/build.gradle.kts. Karena itu gunakan repositori GitHub yang PRIVAT dan jangan membagikannya.
Kunci ini bukan untuk Play Store; untuk Play Store buat kunci rilis sendiri.
APK dari versi sebelum kunci ini ada memakai kunci lain, jadi hapus dulu sekali saja sebelum memasang versi ini.

## Cadangan
Pengaturan > Ekspor menyimpan arti dan keterangan ke file JSON, Impor memulihkannya. Format yang sama dipakai versi web,
jadi file ekspor dari versi web bisa diimpor di sini dan sebaliknya.
