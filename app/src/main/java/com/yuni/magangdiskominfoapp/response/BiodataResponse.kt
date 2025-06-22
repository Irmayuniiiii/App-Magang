package com.yuni.magangdiskominfoapp.response

data class BiodataResponse(
    val success: Boolean,
    val message: String,
    val data: Biodata?
)