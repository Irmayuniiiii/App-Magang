package com.yuni.magangdiskominfoapp.response

data class LamaranResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val error: String? = null
)