# Tugas Praktikum 5 - Desain Pemrograman Berorientasi Objek

## Janji
Saya **Dani Eka Saputra** dengan NIM **2501158** mengerjakan Tugas Praktikum 5 pada Mata Kuliah Desain Pemrograman Berorientasi Objek untuk keberkahan-Nya maka saya tidak melakukan kecurangan seperti yang telah dispesifikasikan. Aamiin

Repository ini merupakan implementasi aplikasi GUI untuk manajemen data person menggunakan Java Swing. Program ini mendemonstrasikan penggunaan konsep OOP (Object-Oriented Programming) dengan GUI untuk operasi CRUD (Create, Read, Update, Delete) pada data person.

Program ini merupakan implementasi dari konsep OOP dengan tema **People (PeopleData)**, yaitu data **pemain profesional (pro player) Valorant** dunia nyata. Program dibuat dengan **Java Swing** (GUI) dan memanfaatkan class **Person** sebagai model data serta class **PeopleMenu** sebagai tampilan dan pengatur logika. 
Program ini merupakan lanjutan dari TP4, data yang diinputkan disimpan ke dalam database dan ditampilkan dalam ```JTable```.

## 🗂️ Struktur Projek
```
.
├── dokumentasi
│   ├── tampilan_awal.png
│   ├── tampilan_tambah.png
│   ├── tampilan_edit.png
│   └── konfirmasi_hapus.png
├── PeopleData
│   ├── Database.java
│   ├── PeopleMenu.form
│   ├── PeopleMenu.java
│   └── Person.java
└── README.md
```

## Fitur Utama
### Java Swing (GUI)
- **Menampilkan** seluruh data pemain pada tabel
- **Menambahkan** data baru melalui form (tombol **Add**)
- **Mengubah** data yang dipilih dari tabel (tombol **Update**)
- **Menghapus** data yang dipilih dari tabel dengan **confirmation prompt** (tombol **Delete**)
- **Membatalkan** pengisian atau pemilihan data (tombol **Cancel**)
- **Validasi input**: semua field wajib diisi, tahun lahir harus angka dan masuk akal (1900 sampai tahun sekarang), role dan skill level wajib dipilih, serta ID tidak boleh ganda
- **Menyimpan data**: data pemain disimpan ke dalam database

## Desain dan Penjelasan

### Desain
![desain](<Dokumentasi/TP5_design.png>)


### Struktur Data Kelas
- #### Person
  | Atribut                       | Keterangan                                                   |
    | ----------------------------- | ------------------------------------------------------------ |
  | **id** *(string)*             | ID pemain, contoh: PL001                                     |
  | **nama** *(string)*           | Nama (nickname) pemain                                       |
  | **negara** *(string)*         | Negara asal pemain                                           |
  | **tahunLahir** *(int)*        | Tahun lahir pemain                                           |
  | **role** *(string)*           | Kategori pemain: Duelist, Controller, Initiator, Sentinel    |
  | **skillLevel** *(string)*     | **Atribut baru**: Regional Champion, Masters Champion, atau World Champion |
  **Method**: konstruktor kosong, konstruktor berparameter, getter setter semua atribut
- #### PeopleMenu
  | Atribut                                  | Keterangan                                              |
    | ---------------------------------------- | ------------------------------------------------------- |
  | **idField, namaField, negaraField, tahunField** *(JTextField)* | Input teks untuk ID, Nama, Negara, dan Tahun Lahir |
  | **roleBox** *(JComboBox)*                | Pilihan role (Kategori)                                 |
  | **regionalChampionRadioButton, mastersRadioButton, wcRadioButton** *(JRadioButton)* | Pilihan skill level (atribut baru) |
  | **skillLevelGroup** *(ButtonGroup)*      | Mengelompokkan radio button agar hanya satu yang terpilih |
  | **addButton** *(JButton)*                | Tombol **Add**, berubah menjadi **Update** saat mode edit |
  | **deleteButton** *(JButton)*             | Tombol **Delete**, hanya tampil saat mode edit          |
  | **cancelButton** *(JButton)*             | Tombol **Cancel**, mengosongkan form                    |
  | **tabelPlayer** *(JTable)*               | Tabel yang menampilkan seluruh data                     |
  | **personList** *(ArrayList&lt;Person&gt;)* | Penyimpan seluruh objek Person                        |
 
- **Method**:

  | Method                 | Keterangan                                                            |
  | ---------------------- | --------------------------------------------------------------------- |
  | readForm()             | Membaca dan memvalidasi isi form, mengembalikan objek Person atau null |
  | insertData()           | Menambah data baru (termasuk pengecekan ID ganda)                     |
  | updateData()           | Mengubah data pada baris yang dipilih                                 |
  | deleteData()           | Menghapus data terpilih setelah konfirmasi Yes/No                     |
  | fillForm()             | Menampilkan data Person yang dipilih ke dalam form                    |
  | clearForm()            | Mengosongkan form dan kembali ke mode tambah                          |
  | setTable()             | Memuat ulang isi tabel dari `personList`                              |
  | setTableSelection()    | Mendeteksi klik baris tabel                                           |
  | setButtonState()       | Mengatur teks tombol dan tampilan tombol Delete sesuai mode           |
  | setComboBox(), setRadioButton(), setButtonAction() | Pengaturan awal component                 |


### Atribut Baru: Skill Level (JRadioButton)

Sesuai ketentuan tugas, ditambahkan satu atribut baru pada class **Person**, yaitu <u>skillLevel</u>, sekaligus component dan logikanya pada form. Atribut ini memakai tiga **JRadioButton** (Regional Champion, Masters Champion, World Champion) yang dimasukkan ke dalam satu **ButtonGroup** bernama <u>skillLevelGroup</u>. Dengan **ButtonGroup**, hanya satu pilihan yang dapat aktif pada satu waktu. Karena component yang dipakai bukan **JTextField**, atribut ini memenuhi syarat bonus pada tugas.

Pada saat menyimpan data, method `readForm()` menelusuri seluruh radio button di grup untuk mencari yang sedang dipilih, lalu mengambil teksnya sebagai nilai <u>skillLevel</u>. Pada saat baris tabel diklik, method `fillForm()` melakukan kebalikannya: radio button yang teksnya sama dengan <u>skillLevel</u> data akan dipilih kembali.

### Mode Tambah dan Mode Edit

Tombol pada form berubah sesuai kondisi tabel:
- **Mode tambah** (tidak ada baris yang dipilih): tombol yang tampil adalah **Add** dan **Cancel**.
- **Mode edit** (baris tabel dipilih): data baris tampil di form, field ID dikunci agar tidak berubah, tombol **Add** berubah menjadi **Update**, dan tombol **Delete** muncul bersama **Cancel**.
  Pengaturan ini dilakukan oleh method `setButtonState()`, yang mengganti teks `addButton` dengan operator ternary (`sedangEdit ? "Update" : "Add"`) dan menampilkan atau menyembunyikan `deleteButton`. Tombol **Cancel** akan mengosongkan form, menghilangkan pilihan baris, lalu mengembalikan tampilan ke mode tambah.

### Confirmation Prompt Sebelum Hapus

Saat tombol **Delete** ditekan, program tidak langsung menghapus data. Method `deleteData()` terlebih dahulu menampilkan dialog konfirmasi (`JOptionPane.showConfirmDialog`) yang berisi nama dan ID pemain yang akan dihapus, dengan pilihan **Yes** dan **No**. Data hanya dihapus dari `personList` jika pengguna memilih **Yes**, kemudian tabel dimuat ulang dan form dikosongkan.

## Dokumentasi
- ### Java Swing
    - #### Tampilan Awal
      ![tampilan_awal](<Dokumentasi/tampilanAwal.png>)
    - #### Tampilan Tambah Data
      ![tambahData](<Dokumentasi/menambahData.png>)
    - #### Tampilan Setelah Menambah Data 
      ![setelahTambah](<Dokumentasi/setelahMenambah.png>)
    - #### Setelah Menambah Data (dalam database) 
      ![databaseSetelahTambah](<Dokumentasi/databaseSetelahMenambah.png>)
    - #### Tampilan Update Data 
      ![setelahTambah](<Dokumentasi/update.png>)
    - #### Tampilan Setelah Update
      ![setelahUpdate](<Dokumentasi/setelahUpdate.png>)
    - #### Setelah Update (dalam database)
      ![databaseSetelahUpdate](<Dokumentasi/databaseSetelahUpdate.png>)
    - #### Tampilan Delete
      ![delete](<Dokumentasi/delete.png>)
    - #### Tampilan Setelah Delete
      ![setelahDelete](<Dokumentasi/sesudahDelete.png>)
    - ####  Setelah Delete (dalam database)
      ![databaseSetelahDelete](<Dokumentasi/databaseSesudahDelete.png>)