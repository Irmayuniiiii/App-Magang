package com.yuni.magangdiskominfoapp.response

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val universitas: String,
    val jurusan: String,
    val semester: Int,
    val role: String,
    val created_at: String,
    val updated_at: String
)