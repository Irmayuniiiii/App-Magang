package com.yuni.magangdiskominfoapp.api

import android.content.Context
import com.google.gson.GsonBuilder
import com.yuni.magangdiskominfoapp.AuthInterceptor
import com.yuni.magangdiskominfoapp.utils.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.greenrobot.eventbus.EventBus
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    const val BASE_URL = "http://192.168.168.128:8000/"
    lateinit var apiService: ApiService
        private set

    fun init(context: Context) {
        createApiService(context)
    }

    fun updateToken(context: Context, newToken: String?) {
        // Simpan token baru
        if (newToken != null) {
            SessionManager.saveToken(context, newToken)
        }
        // Reinisialisasi apiService dengan token yang baru
        createApiService(context)
    }

    private fun createApiService(context: Context) {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = AuthInterceptor(context)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    // Helper function untuk mendapatkan base URL
    fun getBaseUrl(): String {
        return BASE_URL
    }

    // Function untuk mengecek apakah response adalah token expired
    fun isTokenExpired(response: Response<*>): Boolean {
        return response.code() == 401 && response.message().contains("Token has expired", ignoreCase = true)
    }

    // Function untuk menangani token expired
    suspend fun handleTokenExpired(context: Context, onRefreshFailed: () -> Unit) {
        try {
            // Implementasi refresh token bisa ditambahkan di sini
            // Jika gagal refresh token:
            onRefreshFailed()
        } catch (e: Exception) {
            onRefreshFailed()
        }
    }
}