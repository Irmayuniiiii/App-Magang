package com.yuni.magangdiskominfoapp

import android.content.Context
import com.yuni.magangdiskominfoapp.utils.Events
import com.yuni.magangdiskominfoapp.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.Response
import org.greenrobot.eventbus.EventBus

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = SessionManager.getToken(context)
        val request = if (!token.isNullOrEmpty()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/json")
                .build()
        } else {
            originalRequest.newBuilder()
                .header("Accept", "application/json")
                .build()
        }

        val response = chain.proceed(request)

        if (response.code == 401) {
            EventBus.getDefault().post(Events.TokenExpired)
        }

        return response
    }
}