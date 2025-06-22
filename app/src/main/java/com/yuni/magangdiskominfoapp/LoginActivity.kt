package com.yuni.magangdiskominfoapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.yuni.magangdiskominfoapp.api.ApiClient
import com.yuni.magangdiskominfoapp.response.LoginRequest
import com.yuni.magangdiskominfoapp.response.LoginResponse
import com.yuni.magangdiskominfoapp.response.UserData
import com.yuni.magangdiskominfoapp.utils.PhotoProfileManager
import com.yuni.magangdiskominfoapp.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var registerButton: MaterialButton
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is already logged in
        if (SessionManager.isLoggedIn(this) && SessionManager.hasToken(this)) {
            navigateToMain()
            return
        }

        setContentView(R.layout.activity_login)

        ApiClient.init(this)

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)
        progressBar = findViewById(R.id.progressBar)

        // Auto-fill email from registration if available
        intent.getStringExtra("EXTRA_EMAIL")?.let { email ->
            emailEditText.setText(email)
            // Optional: Set focus to password field
            passwordEditText.requestFocus()
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan Password harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performLogin(email, password)
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            // Don't finish() here as user might want to come back
        }
    }

    private fun performLogin(email: String, password: String) {
        progressBar.visibility = View.VISIBLE
        val request = LoginRequest(email, password)

        ApiClient.apiService.loginUser (request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body()?.success == true) {
                    val token = response.body()?.token ?: ""
                    val user: UserData? = response.body()?.user

                    // Save token and user data
                    SessionManager.saveToken(this@LoginActivity, token)
                    user?.let {
                        SessionManager.saveUser (this@LoginActivity, it)
                        SessionManager.saveUsername(this@LoginActivity, it.name)
                    }
                    SessionManager.setLoggedIn(this@LoginActivity, true)

                    // Update token in ApiClient
                    ApiClient.updateToken(this@LoginActivity, token)

                    Toast.makeText(this@LoginActivity, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Login Gagal! Periksa email dan password.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(
                    this@LoginActivity,
                    "Terjadi kesalahan: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun handleLoginSuccess(response: LoginResponse) {
        // Clear any existing photo cache
        PhotoProfileManager.clearProfilePhotoCache(this)

        // Save new user data
        response.user?.let { user ->
            SessionManager.saveUser(this, user)
            user.profile_photo?.let { photoPath ->
                PhotoProfileManager.saveProfilePhotoPath(this, photoPath)
            }
        }

        // Set login status dan token
        SessionManager.setLoggedIn(this, true)
        response.token?.let { SessionManager.saveToken(this, it) }

        // Navigate to MainActivity
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun updateUserInformation(name: String, email: String) {
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("register_name", name)
            putString("register_email", email)
            apply()
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}