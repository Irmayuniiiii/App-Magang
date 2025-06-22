package com.yuni.magangdiskominfoapp.utils

import android.content.Context
import android.view.View
import android.widget.TextView
import com.yuni.magangdiskominfoapp.R

object NavHeaderManager {
    fun updateNavHeader(headerView: View, context: Context) {
        val nameTextView = headerView.findViewById<TextView>(R.id.user_name)
        val emailTextView = headerView.findViewById<TextView>(R.id.user_email)

        // Get user data from SessionManager
        val userData = SessionManager.getUser(context)

        // Update UI elements
        if (userData != null) {
            nameTextView.text = userData.name ?: "User"
            emailTextView.text = userData.email ?: "user@example.com"
        } else {
            // Fallback to SharedPreferences if SessionManager doesn't have the data
            val sharedPref = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val name = sharedPref.getString("register_name", "User")
            val email = sharedPref.getString("register_email", "user@example.com")

            nameTextView.text = name
            emailTextView.text = email
        }
    }
}