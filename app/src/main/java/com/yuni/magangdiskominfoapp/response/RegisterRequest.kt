package com.yuni.magangdiskominfoapp.response

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val password_confirmation: String
)