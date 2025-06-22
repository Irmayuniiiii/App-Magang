package com.yuni.magangdiskominfoapp.response

data class Lamaran(
    val id: Int,
    val user_id: Int,
    val nama: String,
    val email: String,
    val asal_sekolah: String,
    val jurusan: String,
    val semester: Int,
    val tanggal_mulai: String,
    val tanggal_selesai: String,
    val bagian_divisi: String,
    val surat_pengantar_path: String?,
    val cv_path: String?,
    val status: String,
    val surat_diterima_path: String?,
    val surat_ditolak_path: String?,
    val catatan_revisi: String?,
    val sertifikat_path: String?,
    val created_at: String?,
    val updated_at: String?
)

