package com.yuni.magangdiskominfoapp.response

data class LamaranRequest(
    val id: Int,
    val nama: String,
    val email: String,
    val tanggal_mulai: String,
    val tanggal_selesai: String,
    val status: String,
    val catatan_revisi: String?,
    val files: LamaranFiles,
    val pengajuan: String?,
    val created_at: String,
    val updated_at: String
)

data class LamaranFiles(
    val surat_pengantar: String?,
    val cv: String?,
    val surat_diterima: String?,
    val surat_ditolak: String?,
    val sertifikat: String?
)
