package com.yuni.magangdiskominfoapp.utils

sealed class Events {
    object TokenExpired : Events()
}