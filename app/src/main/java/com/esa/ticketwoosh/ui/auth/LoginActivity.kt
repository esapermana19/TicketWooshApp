package com.esa.ticketwoosh.ui.auth

import android.R.attr.textAllCaps
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esa.ticketwoosh.R
import android.view.ViewGroup.LayoutParams
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.esa.ticketwoosh.data.api.ApiClient
import com.esa.ticketwoosh.utils.SessionManager

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. PENGAMAN: Paksa tema AppCompat agar tidak crash/mental saat membuat View lewat kode
        setTheme(androidx.appcompat.R.style.Theme_AppCompat_Light_NoActionBar)

        super.onCreate(savedInstanceState)

        val density = resources.displayMetrics.density
        val wooshRed = resources.getColor(R.color.woosh_red, theme)

        // 2. ScrollView sebagai Root agar layout tidak kepotong di HP layar kecil
        val scrollView = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(Color.parseColor("#F4F6FA"))
            // Perbaikan: Gunakan ViewGroup.LayoutParams (diimpor sebagai LayoutParams)
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        }

        // Layout Utama di dalam ScrollView
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            val padding = (20 * density).toInt()
            setPadding(padding, padding, padding, padding)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        // ==========================================
        // HEADER: Judul Aplikasi Atas (SmartNest Style)
        // ==========================================
        val brandTextView = TextView(this).apply {
            text = "WooshApp"
            textSize = 28f
            setTextColor(wooshRed) // Menggunakan warna merah khas Anda
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (40 * density).toInt()
                bottomMargin = (32 * density).toInt()
            }
        }
        mainLayout.addView(brandTextView)

        // ==========================================
        // FLOATING CARD (Kartu Putih Melengkung Tengah)
        // ==========================================
        val cardLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val cardPadding = (24 * density).toInt()
            setPadding(cardPadding, cardPadding, cardPadding, cardPadding)

            // Membuat background putih melengkung tajam (Radius 28dp seperti di gambar)
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 28 * density
            }

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = (24 * density).toInt()
            }
        }

        // 1. Judul di dalam Kartu
        val welcomeTextView = TextView(this).apply {
            text = "Welcome to\nWoosh login now!"
            textSize = 22f
            setTextColor(Color.parseColor("#1A1A1A"))
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = (28 * density).toInt()
            }
        }
        cardLayout.addView(welcomeTextView)

        // Fungsi pembantu untuk membuat Desain Form Input abu-abu melengkung halus
        fun createModernInputField(hintText: String, isPassword: Boolean): EditText {
            // Label di atas kolom input
            val label = TextView(this@LoginActivity).apply {
                text = hintText.split(" ")[0] // Ambil kata pertama (Email / Password)
                setTextColor(Color.parseColor("#1A1A1A"))
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (6 * density).toInt() }
            }
            cardLayout.addView(label)

            // Kolom Inputnya
            val editText = EditText(this@LoginActivity).apply {
                hint = hintText
                setHintTextColor(Color.parseColor("#A0A5B0"))
                setTextColor(Color.BLACK)
                textSize = 15f
                setPadding((16 * density).toInt(), (14 * density).toInt(), (16 * density).toInt(), (14 * density).toInt())
                inputType = if (isPassword) {
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                } else {
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                }

                // Background abu-abu rounded (seperti di gambar contoh)
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#F0F2F6"))
                    cornerRadius = 14 * density
                }

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (16 * density).toInt() }
            }
            return editText
        }

        // 2. Tambah Input Email & Password ke dalam Kartu
        val emailEditText = createModernInputField("Email Anda", false)
        cardLayout.addView(emailEditText)

        val passwordEditText = createModernInputField("Password Anda", true)
        // Sesuaikan margin bawah password agar pas dengan baris opsi di bawahnya
        (passwordEditText.layoutParams as LinearLayout.LayoutParams).bottomMargin = (12 * density).toInt()
        cardLayout.addView(passwordEditText)

        // ==========================================
        // BARIS OPSI: Remember Me & Forgot Password
        // ==========================================
        val optionsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = (24 * density).toInt() }
        }

        val rememberMeCheckbox = CheckBox(this).apply {
            text = "Remember me"
            setTextColor(Color.parseColor("#8E8E93"))
            textSize = 13f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val forgotPasswordTextView = TextView(this).apply {
            text = "Forget password?"
            setTextColor(Color.parseColor("#4A80FF")) // Warna biru link soft sesuai gambar asli
            textSize = 13f
            gravity = Gravity.END
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        optionsRow.addView(rememberMeCheckbox)
        optionsRow.addView(forgotPasswordTextView)
        cardLayout.addView(optionsRow)

        // ==========================================
        // TOMBOL LOGIN UTAMA (Merah Woosh Bulat)
        // ==========================================
        val loginButton = Button(this).apply {
            text = "Login"
            setTextColor(Color.WHITE)
            textSize = 16f
            isAllCaps = false
            typeface = Typeface.create("sans-serif", Typeface.BOLD)

            // Tombol Melengkung Sempurna berwarna Merah
            background = GradientDrawable().apply {
                setColor(wooshRed)
                cornerRadius = 16 * density
            }

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (52 * density).toInt()
            )
        }
        cardLayout.addView(loginButton)

        // Masukkan seluruh Kartu ke dalam Layout Utama
        mainLayout.addView(cardLayout)

        // ==========================================
        // BOTTOM: Or Sign In With & Social Buttons
        // ==========================================
        val dividerTextView = TextView(this).apply {
            text = "Or Sign in with"
            setTextColor(Color.parseColor("#8E8E93"))
            textSize = 13f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = (20 * density).toInt() }
        }
        mainLayout.addView(dividerTextView)

        // Container Tombol Sosial Media (Horizontal)
        val socialLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Fungsi pembantu membuat tombol buatan lingkaran sosial media
        fun createSocialButton(symbol: String, brandColor: String): TextView {
            return TextView(this@LoginActivity).apply {
                text = symbol
                textSize = 18f
                setTextColor(Color.parseColor(brandColor))
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER

                background = GradientDrawable().apply {
                    setColor(Color.WHITE)
                    cornerRadius = 14 * density // Bentuk kotak melengkung manis (squircle) seperti di gambar
                    setStroke((1 * density).toInt(), Color.parseColor("#E5E5EA")) // Border tipis halus
                }

                layoutParams = LinearLayout.LayoutParams((54 * density).toInt(), (50 * density).toInt()).apply {
                    leftMargin = (10 * density).toInt()
                    rightMargin = (10 * density).toInt()
                }
            }
        }

        // Menambahkan ikon Sosial Media buatan teks (Bisa diganti foto ImageView nanti jika sudah punya asetnya)
        val fbButton = createSocialButton("f", "#3B5998")
        val googleButton = createSocialButton("G", "#DB4437")
        val appleButton = createSocialButton("", "#000000")

        socialLayout.addView(fbButton)
        socialLayout.addView(googleButton)
        socialLayout.addView(appleButton)
        mainLayout.addView(socialLayout)

        // ==========================================
        // PERBAIKAN: Teks Register Pindahkan Ke Sini
        // ==========================================
        val registerTextView = TextView(this).apply {
            text = "Belum punya akun? Daftar di sini" // 1. Perbaikan teks yang terbalik
            setTextColor(Color.parseColor("#4A80FF"))
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, (10 * density).toInt(), 0, (10 * density).toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (24 * density).toInt() // Beri sedikit jarak di bawah tombol social media
            }
        }

        // 2. KUNCI PERBAIKAN: Masukkan teks ke dalam layout utama agar muncul di layar
        mainLayout.addView(registerTextView)

        // Logika Klik Teks Register
        registerTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Baru setelah semua komponen masuk ke mainLayout, masukkan ke scrollView
        scrollView.addView(mainLayout)
        setContentView(scrollView)

        // ==========================================
        // LOGIKA AKSI KLIK TOMBOL
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

                            // 1. Simpan Token ke SharedPreferences
                            loginData.token?.let { token ->
                                sessionManager.saveAuthToken(token)
                            }

                            Toast.makeText(this@LoginActivity, "Login Sukses! Selamat datang ${loginData.user?.fullName}", Toast.LENGTH_LONG).show()

                            // 2. PINDAH KE DASHBOARD / HALAMAN UTAMA
                            // Sementara kita buat toast dulu, nanti kita ganti ke DashboardActivity Anda
                            Toast.makeText(this@LoginActivity, "Masuk ke Dashboard...", Toast.LENGTH_SHORT).show()

                        } else {
                            val errorJson = response.errorBody()?.string()
                            Toast.makeText(this@LoginActivity, "Laravel Reject: $errorJson", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@LoginActivity, "Error Koneksi: Server tidak merespon!", Toast.LENGTH_LONG).show()
                    } finally {
                        loginButton.isEnabled = true
                        loginButton.text = "Login"
                    }
                }
            }
        }
    }
}