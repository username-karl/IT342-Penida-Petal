package com.petal.features.user

import android.content.Intent
import com.petal.R
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.petal.shared.network.ApiClient
import com.petal.shared.util.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvGreeting: TextView
    private lateinit var tvWelcomeName: TextView
    private lateinit var ivAvatar: ImageView
    private lateinit var ivPopular1: ImageView
    private lateinit var ivPopular2: ImageView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var btnLogout: Button

    private lateinit var buyerContent: android.widget.LinearLayout
    private lateinit var floristContent: android.widget.LinearLayout

    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        tokenManager = TokenManager(this)

        tvWelcomeName = findViewById(R.id.tvWelcomeName)
        ivAvatar = findViewById(R.id.ivAvatar)
        ivPopular1 = findViewById(R.id.ivPopular1)
        ivPopular2 = findViewById(R.id.ivPopular2)
        bottomNav = findViewById(R.id.bottomNav)
        btnLogout = findViewById(R.id.btnLogout)
        buyerContent = findViewById(R.id.buyerContent)
        floristContent = findViewById(R.id.floristContent)

        // Set dynamic greeting based on time
        val greetingView = findViewById<TextView>(R.id.tvGreeting)
        if (greetingView != null) {
            greetingView.text = getTimeBasedGreeting()
        }

        applyRoleUI(tokenManager.getRole())

        // Load placeholder image for avatar
        Glide.with(this)
            .load("https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=250&auto=format&fit=crop")
            .centerCrop()
            .into(ivAvatar)

        // Load mock bouquet images
        Glide.with(this)
            .load("https://images.unsplash.com/photo-1596627672288-757c91799e0c?q=80&w=400&auto=format&fit=crop")
            .centerCrop()
            .into(ivPopular1)

        Glide.with(this)
            .load("https://images.unsplash.com/photo-1563241598-6ce3b266e744?q=80&w=400&auto=format&fit=crop")
            .centerCrop()
            .into(ivPopular2)

        // Bottom nav listener
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> performLogout() // Quick action to logout for debug
            }
            true
        }

        fetchUserProfile()

        btnLogout.setOnClickListener {
            performLogout()
        }
    }

    private fun getTimeBasedGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good morning,"
            hour < 17 -> "Good afternoon,"
            else -> "Good evening,"
        }
    }

    private fun applyRoleUI(role: String?) {
        if (role == "ROLE_FLORIST" || role == "artisan") {
            buyerContent.visibility = android.view.View.GONE
            floristContent.visibility = android.view.View.VISIBLE
        } else {
            buyerContent.visibility = android.view.View.VISIBLE
            floristContent.visibility = android.view.View.GONE
        }
    }

    private fun fetchUserProfile() {
        val apiService = ApiClient.getClient(tokenManager)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getCurrentUser()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val user = response.body()?.data
                        val firstName = user?.name?.split(" ")?.get(0) ?: "Customer"
                        tvWelcomeName.text = firstName

                        val role = user?.role
                        if (role != null) {
                            tokenManager.saveRole(role)
                            applyRoleUI(role)
                        }
                    } else {
                        Toast.makeText(this@DashboardActivity, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
                        forceLogoutLocally()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DashboardActivity, "Could not load profile. Check your connection.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun performLogout() {
        val apiService = ApiClient.getClient(tokenManager)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Inform backend of logout
                val response = apiService.logout()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@DashboardActivity, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    }
                    forceLogoutLocally()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    forceLogoutLocally() // Still clear local token even if network call fails
                }
            }
        }
    }

    private fun forceLogoutLocally() {
        tokenManager.clearToken()
        val intent = Intent(this, com.petal.features.auth.LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
