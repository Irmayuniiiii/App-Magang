package com.yuni.magangdiskominfoapp.response

data class UserProfile(
    val name: String,
    val email: String,
    val tempatLahir: String,
    val tanggalLahir: String,
    val jenisKelamin: String,
    val agama: String,
    val alamat: String,
    val institusi: String,
    val jurusan: String,
    val semester: String,
    val ipk: String
)
