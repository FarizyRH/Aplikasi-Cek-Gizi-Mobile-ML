# GoCheck – Aplikasi Cek Gizi Mobile & Machine Learning

GoCheck adalah aplikasi mobile berbasis Android yang dirancang untuk membantu pengguna memahami kategori nutrisi produk kemasan secara lebih mudah. Aplikasi ini memungkinkan pengguna melakukan pengecekan produk melalui input manual, pencarian produk, pemindaian barcode, serta melihat riwayat hasil pengecekan.

Project ini dikembangkan sebagai proyek akademik pada Program Studi Teknik Informatika, Politeknik Caltex Riau.

## Deskripsi Project

Informasi nilai gizi pada produk kemasan sering kali sulit dipahami oleh masyarakat umum karena ditampilkan dalam bentuk angka dan istilah nutrisi seperti kalori, gula, lemak, protein, karbohidrat, dan natrium. GoCheck dibuat untuk membantu pengguna memperoleh informasi kategori nutrisi produk secara lebih sederhana dan praktis.

Aplikasi ini mengintegrasikan fitur mobile dengan machine learning untuk membantu proses pengkategorian nutrisi produk. Pengguna dapat memasukkan data nutrisi secara manual, melakukan pencarian produk, memindai barcode produk, serta menyimpan hasil pengecekan ke dalam riwayat.

## Tujuan Project

* Membantu pengguna memahami kategori nutrisi produk kemasan.
* Menyediakan fitur pengecekan nutrisi melalui input manual.
* Menyediakan fitur scan barcode untuk mencari informasi produk.
* Menyediakan fitur pencarian produk secara manual.
* Menampilkan hasil kategori nutrisi secara sederhana dan mudah dipahami.
* Menyimpan riwayat hasil pengecekan produk.
* Mengintegrasikan aplikasi mobile dengan model machine learning.

## Fitur Utama

* Splash screen
* Home screen
* Input manual data nutrisi
* Scan barcode produk
* Pencarian produk
* Detail hasil pencarian
* Prediksi/kategorisasi nutrisi
* Riwayat hasil pengecekan
* Detail riwayat pengecekan
* Penyimpanan data lokal menggunakan Room Database
* Integrasi model machine learning berbasis ONNX
* Integrasi API menggunakan OkHttp dan Gson

## Teknologi yang Digunakan

* Kotlin
* Android Studio
* Android SDK
* Gradle Kotlin DSL
* XML Layout
* ViewBinding
* CameraX
* ML Kit Barcode Scanning
* ONNX Runtime
* Room Database
* Coroutines
* OkHttp
* Gson
* RecyclerView
* CardView
* Material Components
* Navigation Component
* Git & GitHub

## Machine Learning

Aplikasi ini menggunakan model machine learning dalam format ONNX yang ditempatkan pada folder `assets`. Model digunakan untuk membantu proses pengkategorian nutrisi berdasarkan input data produk.

Model file:

```text
app/src/main/assets/model_cluster.onnx
```

Model machine learning digunakan sebagai bagian dari proses analisis nutrisi agar aplikasi dapat memberikan hasil kategori yang lebih mudah dipahami oleh pengguna.

## Struktur Repository

```bash
Aplikasi-Cek-Gizi-Mobile-ML/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── assets/
│   │       │   └── model_cluster.onnx
│   │       ├── java/com/example/gocheck/
│   │       │   ├── adapter/
│   │       │   ├── database/
│   │       │   ├── ml/
│   │       │   ├── model/
│   │       │   ├── network/
│   │       │   ├── repository/
│   │       │   ├── ui/
│   │       │   └── utils/
│   │       ├── res/
│   │       │   └── layout/
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── .gitignore
└── README.md
```

## Struktur Package

| Package      | Fungsi                                                                    |
| ------------ | ------------------------------------------------------------------------- |
| `adapter`    | Mengatur adapter untuk tampilan list seperti riwayat atau hasil pencarian |
| `database`   | Mengelola database lokal aplikasi                                         |
| `ml`         | Mengelola proses machine learning dan pemanggilan model                   |
| `model`      | Menyimpan data class/model yang digunakan aplikasi                        |
| `network`    | Mengelola komunikasi API atau pengambilan data eksternal                  |
| `repository` | Menghubungkan sumber data dari database, network, dan logic aplikasi      |
| `ui`         | Mengelola tampilan dan interaksi pengguna                                 |
| `utils`      | Menyimpan fungsi bantuan atau utility                                     |

## Tampilan Aplikasi

Beberapa halaman utama dalam aplikasi:

* Home
* Input Manual
* Scan Barcode
* Search
* Riwayat
* Detail Riwayat

Layout utama aplikasi berada pada folder:

```text
app/src/main/res/layout/
```

Beberapa file layout yang digunakan:

```text
activity_main.xml
fragment_home.xml
fragment_input.xml
fragment_kamera.xml
fragment_search.xml
fragment_history.xml
fragment_detail_history.xml
item_search.xml
item_history.xml
```

## Peran Saya

**Farizy Rahman Hidayat**
**Role:** Mobile Application Contributor / Functional Analyst / User Flow Designer

Kontribusi saya dalam project ini meliputi:

* Berkontribusi dalam pengembangan aplikasi mobile Android.
* Merancang alur fungsional aplikasi berdasarkan kebutuhan pengguna.
* Membantu perancangan fitur input manual, scan barcode, search, hasil kategori, dan riwayat.
* Mendukung integrasi model machine learning ke dalam aplikasi.
* Membantu pengelolaan alur data antara UI, repository, database, network, dan model ML.
* Berkontribusi dalam penyusunan dokumentasi project, use case, dan alur penggunaan aplikasi.
* Melakukan pengujian fungsional terhadap fitur utama aplikasi.

## Tim Project

* Cristiano
* Farizy Rahman Hidayat

## Cara Menjalankan Project

1. Clone repository:

```bash
git clone https://github.com/FarizyRH/Aplikasi-Cek-Gizi-Mobile-ML.git
```

2. Buka project menggunakan Android Studio.

3. Tunggu proses Gradle Sync selesai.

4. Pastikan Android Studio menggunakan JDK yang sesuai dengan konfigurasi project.

5. Jalankan aplikasi menggunakan emulator Android atau perangkat Android fisik.

6. Klik tombol **Run** pada Android Studio.

## Konfigurasi Minimum

Konfigurasi aplikasi:

```text
Minimum SDK: 24
Target SDK: 36
Compile SDK: 36
Application ID: com.example.gocheck
Version: 1.0
```

## Catatan Pengembangan

Project ini menggunakan beberapa library utama:

* CameraX untuk akses kamera.
* ML Kit Barcode Scanning untuk pemindaian barcode.
* ONNX Runtime untuk menjalankan model machine learning.
* Room Database untuk penyimpanan lokal.
* OkHttp dan Gson untuk komunikasi dan pemrosesan data API.
* Coroutines untuk proses asynchronous.
* ViewBinding untuk menghubungkan layout XML dengan kode Kotlin.

## Status Project

Status: **Academic Project / Prototype**

Project ini dibuat untuk kebutuhan akademik dan pembelajaran. Aplikasi belum ditujukan sebagai sistem produksi penuh, tetapi dapat digunakan sebagai prototype aplikasi pengecekan kategori nutrisi produk kemasan berbasis mobile dan machine learning.

## Catatan Keamanan

Jangan menyimpan data sensitif di repository publik, seperti:

* API key
* Token akses
* Password
* File credential
* Dataset pribadi
* Data pengguna asli

Jika menggunakan API eksternal, gunakan file konfigurasi lokal yang tidak dipush ke GitHub.

## Lisensi

Project ini dibuat untuk kebutuhan akademik. Penggunaan ulang kode atau dokumen dapat disesuaikan dengan persetujuan anggota kelompok.
