    package com.yuni.magangdiskominfoapp

    import android.content.Intent
    import android.os.Bundle
    import android.widget.Toast
    import androidx.appcompat.app.AppCompatActivity
    import com.yuni.magangdiskominfoapp.api.ApiClient
    import com.yuni.magangdiskominfoapp.response.RegisterResponse
    import com.google.android.material.button.MaterialButton
    import com.google.android.material.textfield.TextInputEditText
    import com.yuni.magangdiskominfoapp.response.RegisterRequest
    import retrofit2.Call
    import retrofit2.Callback
    import retrofit2.Response

    class RegisterActivity : AppCompatActivity() {

        private lateinit var nameEditText: TextInputEditText
        private lateinit var emailEditText: TextInputEditText
        private lateinit var passwordEditText: TextInputEditText
        private lateinit var confirmPasswordEditText: TextInputEditText
        private lateinit var registerButton: MaterialButton
        private lateinit var loginButton: MaterialButton

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_register)

            ApiClient.init(this)

            // Initialize views
            nameEditText = findViewById(R.id.nameEditText)
            emailEditText = findViewById(R.id.emailEditText)
            passwordEditText = findViewById(R.id.passwordEditText)
            confirmPasswordEditText = findViewById(R.id.passwordConfirmEditText)
            registerButton = findViewById(R.id.registerButton)
            loginButton = findViewById(R.id.loginButton)

            registerButton.setOnClickListener {
                registerUser()
            }

            loginButton.setOnClickListener {
                // Navigasi ke login tanpa data karena user belum register
                navigateToLogin()
            }
        }

        private fun registerUser() {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Semua kolom harus diisi!", Toast.LENGTH_SHORT).show()
                return
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Password dan Konfirmasi Password tidak cocok!", Toast.LENGTH_SHORT).show()
                return
            }

            val registerRequest = RegisterRequest(name, email, password, confirmPassword)

            ApiClient.apiService.registerUser(registerRequest)
                .enqueue(object : Callback<RegisterResponse> {
                    override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                        if (response.isSuccessful) {
                            // Simpan nama dan email ke SharedPreferences
                            val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putString("register_name", name)
                                putString("register_email", email)
                                apply()
                            }

                            Toast.makeText(this@RegisterActivity, "Registrasi Berhasil! Silakan login.", Toast.LENGTH_SHORT).show()
                            // Navigasi ke login dengan data setelah registrasi berhasil
                            navigateToLoginWithData(name, email)
                        } else {
                            Toast.makeText(this@RegisterActivity, "Registrasi Gagal! Coba lagi.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                        Toast.makeText(this@RegisterActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        // Fungsi untuk navigasi ke login tanpa data (digunakan untuk tombol "Sudah punya akun?")
        private fun navigateToLogin() {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Fungsi untuk navigasi ke login dengan data (digunakan setelah registrasi berhasil)
        private fun navigateToLoginWithData(name: String, email: String) {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("EXTRA_EMAIL", email)
            }
            startActivity(intent)
            finish()
        }
    }