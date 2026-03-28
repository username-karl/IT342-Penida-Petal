package com.petal

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.petal.network.ApiClient
import com.petal.util.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView
    private lateinit var tvError: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var rgRole: android.widget.RadioGroup
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        tokenManager = TokenManager(this)

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        rgRole = findViewById(R.id.rgRole)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)
        tvError = findViewById(R.id.tvError)
        progressBar = findViewById(R.id.progressBar)

        btnRegister.setOnClickListener {
            clearError()
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validate name
            if (name.isEmpty()) {
                showError("Please enter your full name")
                etName.requestFocus()
                return@setOnClickListener
            }

            if (name.length < 2) {
                showError("Name must be at least 2 characters")
                etName.requestFocus()
                return@setOnClickListener
            }

            // Validate email
            if (email.isEmpty()) {
                showError("Please enter your email address")
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showError("Please enter a valid email address")
                etEmail.requestFocus()
                return@setOnClickListener
            }

            // Validate password
            if (password.isEmpty()) {
                showError("Please enter a password")
                etPassword.requestFocus()
                return@setOnClickListener
            }

            if (password.length < 6) {
                showError("Password must be at least 6 characters")
                etPassword.requestFocus()
                return@setOnClickListener
            }

            val role = if (rgRole.checkedRadioButtonId == R.id.rbArtisan) "artisan" else "customer"

            performRegistration(name, email, password, role)
        }

        tvLogin.setOnClickListener {
            finish() // Go back to login screen
        }
    }

    private fun performRegistration(name: String, email: String, pass: String, role: String) {
        setLoading(true)
        val requestBody = mapOf(
            "name" to name,
            "email" to email,
            "password" to pass,
            "role" to role
        )
        val apiService = ApiClient.getClient(tokenManager)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.register(requestBody)
                withContext(Dispatchers.Main) {
                    setLoading(false)
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@RegisterActivity, "Registration Successful! Please login.", Toast.LENGTH_LONG).show()
                        finish() // Return to Login Screen on success
                    } else {
                        val errorMessage = response.body()?.message ?: "Registration failed. Please try again."
                        showError(errorMessage)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    setLoading(false)
                    showError("Connection failed. Please check your network and ensure the server is running.")
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnRegister.isEnabled = !loading
        btnRegister.alpha = if (loading) 0.6f else 1.0f
        tvLogin.isEnabled = !loading
        etName.isEnabled = !loading
        etEmail.isEnabled = !loading
        etPassword.isEnabled = !loading
    }

    private fun showError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    private fun clearError() {
        tvError.visibility = View.GONE
        tvError.text = ""
    }
}
