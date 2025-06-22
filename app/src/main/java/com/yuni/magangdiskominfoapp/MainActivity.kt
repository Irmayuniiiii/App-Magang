package com.yuni.magangdiskominfoapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.yuni.magangdiskominfoapp.api.ApiClient
import com.yuni.magangdiskominfoapp.response.LogoutResponse
import com.yuni.magangdiskominfoapp.utils.NavHeaderManager
import com.yuni.magangdiskominfoapp.utils.PhotoProfileManager
import com.yuni.magangdiskominfoapp.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.init(this)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize API and Token
        initializeApiAndToken()

        // Initialize Views
        initializeViews()

        // Setup Navigation Drawer
        setupNavigationDrawer()

        // Load Default Fragment
        if (savedInstanceState == null) {
            replaceFragment(DashboardFragment(), addToBackStack = false)
        }

        // Setup Edge-to-Edge Display
        setupEdgeToEdgeDisplay()
    }

    private fun initializeApiAndToken() {
        val token = SessionManager.getToken(this)
        ApiClient.updateToken(this, token)
    }

    private fun initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    private fun setupNavigationDrawer() {
        // Setup Toggle
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, findViewById(R.id.toolbar),
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Setup Navigation Header
        setupNavigationHeader()

        // Setup Navigation Items
        setupNavigationItems()

        // Update Navigation Header with User Information
        val headerView = navigationView.getHeaderView(0)
        NavHeaderManager.updateNavHeader(headerView, this)
    }

    private fun setupNavigationItems() {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> {
                    replaceFragment(DashboardFragment(), addToBackStack = false)
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_biodata -> {
                    replaceFragment(BiodataFragment(), addToBackStack = false)
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_lamaran -> {
                    replaceFragment(LamaranFragment(), addToBackStack = false)
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_logout -> {
                    drawerLayout.closeDrawers()
                    performLogout()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupEdgeToEdgeDisplay() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun performLogout() {
        val token = SessionManager.getToken(this)
        if (token != null) {
            ApiClient.apiService.logoutUser("Bearer $token")
                .enqueue(object : Callback<LogoutResponse> {
                    override fun onResponse(
                        call: Call<LogoutResponse>,
                        response: Response<LogoutResponse>
                    ) {
                        if (response.isSuccessful) {
                            handleLogoutComplete()
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Logout gagal: ${response.message()}",
                                Toast.LENGTH_SHORT
                            ).show()
                            // Tetap logout dari aplikasi meskipun API gagal
                            handleLogoutComplete()
                        }
                    }

                    override fun onFailure(call: Call<LogoutResponse>, t: Throwable) {
                        Toast.makeText(
                            this@MainActivity,
                            "Error: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Tetap logout dari aplikasi meskipun API gagal
                        handleLogoutComplete()
                    }
                })
        } else {
            handleLogoutComplete()
        }
    }

    private fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
        if (addToBackStack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()
    }
    override fun onResume() {
        super.onResume()
        // Update navigation header setiap kali activity di-resume
        updateNavigationHeader()
    }
    private fun setupNavigationHeader() {
        val headerView = navigationView.getHeaderView(0)
        val profileImage = headerView.findViewById<ImageView>(R.id.ivProfilePhoto)
        val userName = headerView.findViewById<TextView>(R.id.user_name)
        val userEmail = headerView.findViewById<TextView>(R.id.user_email)

        // Load foto profil dengan skip cache
        PhotoProfileManager.loadProfilePhoto(this, profileImage)

        // Set nama dan email dari SessionManager
        SessionManager.getUser(this)?.let { userData ->
            userName.text = userData.name
            userEmail.text = userData.email
        }
    }

    fun updateNavigationHeader() {
        val headerView = navigationView.getHeaderView(0)
        val profileImage = headerView.findViewById<ImageView>(R.id.ivProfilePhoto)
        val userName = headerView.findViewById<TextView>(R.id.user_name)
        val userEmail = headerView.findViewById<TextView>(R.id.user_email)

        // Update foto profil
        PhotoProfileManager.loadProfilePhoto(this, profileImage)

        // Update user info
        SessionManager.getUser(this)?.let { userData ->
            userName.text = userData.name
            userEmail.text = userData.email
        }
    }

    private fun handleLogoutComplete() {
        // Clear session dan cache
        SessionManager.clearSession(this)
        SessionManager.setLoggedIn(this, false)
        PhotoProfileManager.clearProfilePhotoCache(this)

        Toast.makeText(this, "Berhasil logout", Toast.LENGTH_SHORT).show()

        // Navigasi ke LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}