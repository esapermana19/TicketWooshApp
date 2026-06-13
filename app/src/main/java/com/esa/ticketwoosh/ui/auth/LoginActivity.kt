package com.esa.ticketwoosh.ui.auth

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View // Add this import
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.esa.ticketwoosh.R
import com.esa.ticketwoosh.data.api.ApiClient
import com.esa.ticketwoosh.ui.booking.BookingActivity
import com.esa.ticketwoosh.utils.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val density = resources.displayMetrics.density
        val wooshRed = Color.parseColor("#ED1C24")
        val textColorPrimary = Color.parseColor("#1C1C1E")
        val textColorSecondary = Color.parseColor("#6C757D")
        val inputBorderColor = Color.parseColor("#CED4DA")

        // ScrollView sebagai Root
        val scrollView = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(Color.WHITE)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            val padding = (24 * density).toInt()
            setPadding(padding, (60 * density).toInt(), padding, padding)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // ==========================================
        // 1. Logo
        // ==========================================
        val logoImageView = ImageView(this).apply {
            setImageResource(R.drawable.logo11merah)
            layoutParams = LinearLayout.LayoutParams(
                (150 * density).toInt(),
                (150 * density).toInt()
            ).apply {
                bottomMargin = (24 * density).toInt()
            }
        }
        mainLayout.addView(logoImageView)

        // ==========================================
        // 2. Titles
        // ==========================================
        val welcomeTitle = TextView(this).apply {
            text = "Selamat Datang Kembali"
            textSize = 22f
            setTextColor(textColorPrimary)
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        mainLayout.addView(welcomeTitle)

        val welcomeSubtitle = TextView(this).apply {
            text = "Masuk untuk memesan tiket perjalanan Anda"
            textSize = 14f
            setTextColor(textColorSecondary)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = (32 * density).toInt()
                topMargin = (4 * density).toInt()
            }
        }
        mainLayout.addView(welcomeSubtitle)

        // ==========================================
        // 3. Form Input Helper
        // ==========================================
        fun getInputBorder(): GradientDrawable {
            return GradientDrawable().apply {
                setColor(Color.WHITE)
                setStroke((1 * density).toInt(), inputBorderColor)
                cornerRadius = 10 * density
            }
        }

        // Email Section
        val emailLabel = TextView(this).apply {
            text = "Email atau Nomor Ponsel"
            textSize = 13f
            setTextColor(textColorSecondary)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = (8 * density).toInt() }
        }
        mainLayout.addView(emailLabel)

        val emailEditText = EditText(this).apply {
            hint = "contoh@email.com"
            textSize = 15f
            setTextColor(Color.BLACK)
            setPadding((16 * density).toInt(), (14 * density).toInt(), (16 * density).toInt(), (14 * density).toInt())
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            background = getInputBorder()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = (20 * density).toInt() }
        }
        mainLayout.addView(emailEditText)

        // Password Section Layout (For Label and Forgot Password)
        val passwordLabelLayout = RelativeLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = (8 * density).toInt() }
        }

        val passwordLabel = TextView(this).apply {
            text = "Kata Sandi"
            textSize = 13f
            setTextColor(textColorSecondary)
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply { addRule(RelativeLayout.ALIGN_PARENT_LEFT) }
        }
        passwordLabelLayout.addView(passwordLabel)

        val forgotPassword = TextView(this).apply {
            text = "Lupa Sandi?"
            textSize = 13f
            setTextColor(wooshRed)
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply { addRule(RelativeLayout.ALIGN_PARENT_RIGHT) }
        }
        passwordLabelLayout.addView(forgotPassword)
        mainLayout.addView(passwordLabelLayout)

        val passwordEditText = EditText(this).apply {
            hint = "••••••••"
            textSize = 15f
            setTextColor(Color.BLACK)
            setPadding((16 * density).toInt(), (14 * density).toInt(), (16 * density).toInt(), (14 * density).toInt())
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            background = getInputBorder()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = (16 * density).toInt() }
        }
        mainLayout.addView(passwordEditText)

        // ==========================================
        // 4. Remember Me
        // ==========================================
        val rememberMeCheckbox = CheckBox(this).apply {
            text = "Ingat saya di perangkat ini"
            setTextColor(textColorSecondary)
            buttonTintList = android.content.res.ColorStateList.valueOf(wooshRed)
            textSize = 13f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = (32 * density).toInt() }
        }
        mainLayout.addView(rememberMeCheckbox)

        // ==========================================
        // 5. Login Button
        // ==========================================
        val loginButton = Button(this).apply {
            text = "Masuk Sekarang →"
            setTextColor(Color.WHITE)
            textSize = 16f
            isAllCaps = false
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            background = GradientDrawable().apply {
                setColor(wooshRed)
                cornerRadius = 10 * density
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (54 * density).toInt()
            )
        }
        mainLayout.addView(loginButton)

        // Spacer to push register text to bottom
        val spacer = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        mainLayout.addView(spacer)

        // ==========================================
        // 6. Register Text
        // ==========================================
        val registerTextView = TextView(this).apply {
            text = "Belum punya akun? Daftar Gratis"
            setTextColor(textColorSecondary)
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, (20 * density).toInt(), 0, (20 * density).toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (40 * density).toInt()
            }
            setOnClickListener {
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
            }
        }
        mainLayout.addView(registerTextView)

        scrollView.addView(mainLayout)
        setContentView(scrollView)

        // ==========================================
        // LOGIC
        // ==========================================
        val sessionManager = SessionManager(this)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password wajib diisi!", Toast.LENGTH_SHORT).show()
            } else {
                loginButton.isEnabled = false
                loginButton.text = "Loading..."

                lifecycleScope.launch {
                    try {
                        val requestBody = hashMapOf(
                            "email" to email,
                            "password_hash" to password
                        )

                        val response = ApiClient.instance.loginUser(requestBody)

                        if (response.isSuccessful && response.body() != null) {
                            val loginData = response.body()!!

                            loginData.token?.let { token ->
                                sessionManager.saveAuthToken(token, loginData.user?.fullName ?: "Penumpang")
                            }

                            Toast.makeText(this@LoginActivity, "Login Sukses!", Toast.LENGTH_LONG).show()

                            val intent = Intent(this@LoginActivity, BookingActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, "Login gagal, periksa email dan password.", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@LoginActivity, "Error: Koneksi server gagal", Toast.LENGTH_LONG).show()
                    } finally {
                        loginButton.isEnabled = true
                        loginButton.text = "Masuk Sekarang →"
                    }
                }
            }
        }
    }
}