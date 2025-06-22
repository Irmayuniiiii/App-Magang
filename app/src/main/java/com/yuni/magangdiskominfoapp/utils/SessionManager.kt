package com.yuni.magangdiskominfoapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.yuni.magangdiskominfoapp.response.UserData

object SessionManager {
    private const val PREF_NAME = "AuthPrefs"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_USER = "user_data"
    private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    private const val KEY_USERNAME = "username"

    // Tambahkan fungsi untuk menyimpan username
    fun saveUsername(context: Context, username: String) {
        getPrefs(context).edit().putString(KEY_USERNAME, username).apply()
    }

    // Tambahkan fungsi untuk mengambil username
    fun getUsername(context: Context): String? {
        return getPrefs(context).getString(KEY_USERNAME, null)
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(context: Context, token: String) {
        Log.d("SessionManager", "Menyimpan token: $token")
        getPrefs(context).edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(context: Context): String? {
        val token = getPrefs(context).getString(KEY_TOKEN, null)
        Log.d("SessionManager", "Mengambil token: $token")
        return token
    }

    fun hasToken(context: Context): Boolean {
        return !getToken(context).isNullOrEmpty()
    }

    fun saveUser(context: Context, user: UserData) {
        val userJson = Gson().toJson(user)
        getPrefs(context).edit().putString(KEY_USER, userJson).apply()
    }

    fun getUser(context: Context): UserData? {
        val userJson = getPrefs(context).getString(KEY_USER, null)
        return if (userJson != null) Gson().fromJson(userJson, UserData::class.java) else null
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }
    fun clearSession(context: Context) {
        Log.d("SessionManager", "Menghapus sesi pengguna.")
        // Clear photo cache when clearing session
        PhotoProfileManager.clearProfilePhotoCache(context)
        getPrefs(context).edit().clear().apply()
    }

    fun setLoggedIn(context: Context, isLoggedIn: Boolean) {
        if (!isLoggedIn) {
            // Clear photo cache when logging out
            PhotoProfileManager.clearProfilePhotoCache(context)
        }
        getPrefs(context).edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }

}