package com.yuni.magangdiskominfoapp.response

data class Biodata(
    val id: Int,
    val user_id: Int,
    val nama_lengkap: String,
    val tempat_lahir: String?,
    val tanggal_lahir: String?,
    val jenis_kelamin: String?,
    val agama: String?,
    val alamat: String?,
    val asal_sekolah: String?,
    val jurusan: String?,
    val semester: Int?,
    val ipk: Double?,
    val profile_photo: String?,
    val created_at: String?,
    val updated_at: String?
)