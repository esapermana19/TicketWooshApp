package com.esa.ticketwoosh.ui.auth

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esa.ticketwoosh.R
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.esa.ticketwoosh.data.api.ApiClient

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. PENGAMAN: Paksa tema AppCompat agar tidak crash saat inflate view via kode
        setTheme(androidx.appcompat.R.style.Theme_AppCompat_Light_NoActionBar)

        super.onCreate(savedInstanceState)

        val density = resources.displayMetrics.density
        val wooshRed = resources.getColor(R.color.woosh_red, theme)

        // 2. ScrollView sebagai Root
        val scrollView = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(Color.parseColor("#F4F6FA"))
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
        // HEADER: Judul Aplikasi Atas
        // ==========================================
        val brandTextView = TextView(this).apply {
            text = "WooshApp"
            textSize = 28f
            setTextColor(wooshRed)
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (30 * density).toInt()
                bottomMargin = (24 * density).toInt()
            }
        }
        mainLayout.addView(brandTextView)

        // ==========================================
        // FLOATING CARD (Kartu Putih Melengkung)
        // ==========================================
        val cardLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val cardPadding = (24 * density).toInt()
            setPadding(cardPadding, cardPadding, cardPadding, cardPadding)

            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 28 * density // Lengkungan sudut kartu (28dp)
            }

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = (24 * density).toInt()
            }
        }

        // 1. Judul Kartu
        val titleCardTextView = TextView(this).apply {
            text = "Create an Account?"
            textSize = 22f
            setTextColor(Color.parseColor("#1A1A1A"))
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = (24 * density).toInt()
            }
        }
        cardLayout.addView(titleCardTextView)

        // Fungsi pembantu membuat Input Field modern dengan label di atasnya
        fun createInputField(hintText: String, inputTypeEnum: Int): EditText {
            val label = TextView(this@RegisterActivity).apply {
                text = hintText.split(" ")[0] // Ambil kata pertama (Name / Email / Phone / Password)
                setTextColor(Color.parseColor("#1A1A1A"))
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (6 * density).toInt() }
            }
            cardLayout.addView(label)

            val editText = EditText(this@RegisterActivity).apply {
                hint = hintText
                setHintTextColor(Color.parseColor("#A0A5B0"))
                setTextColor(Color.BLACK)
                textSize = 15f
                setPadding((16 * density).toInt(), (14 * density).toInt(), (16 * density).toInt(), (14 * density).toInt())
                inputType = inputTypeEnum

                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#F0F2F6")) // Warna abu-abu halus input
                    cornerRadius = 14 * density
                }

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (16 * density).toInt() }
            }
            return editText
        }

        // 2. Tambah Input Fields (Name, Email, Phone, Password)
        val nameEditText = createInputField("Name Anda", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PERSON_NAME)
        cardLayout.addView(nameEditText)

        val emailEditText = createInputField("Email Anda", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        cardLayout.addView(emailEditText)

        // BARU: Kolom input nomor HP/Telepon dengan keyboard khusus angka telepon
        val phoneEditText = createInputField("Phone Anda", InputType.TYPE_CLASS_PHONE)
        cardLayout.addView(phoneEditText)

        val passwordEditText = createInputField("Password Anda", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        (passwordEditText.layoutParams as LinearLayout.LayoutParams).bottomMargin = (12 * density).toInt()
        cardLayout.addView(passwordEditText)

        // ==========================================
        // CHECBOX: I agree to Terms of Service
        // ==========================================
        val termsCheckbox = CheckBox(this).apply {
            text = "I agree to the Terms of Service"
            setTextColor(Color.parseColor("#8E8E93"))
            textSize = 13f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = (24 * density).toInt() }
        }
        cardLayout.addView(termsCheckbox)

        // ==========================================
        // TOMBOL REGISTER (Merah Woosh Bulat)
        // ==========================================
        val registerButton = Button(this).apply {
            text = "Create account"
            setTextColor(Color.WHITE)
            textSize = 16f
            isAllCaps = false
            typeface = Typeface.create("sans-serif", Typeface.BOLD)

            background = GradientDrawable().apply {
                setColor(wooshRed)
                cornerRadius = 16 * density
            }

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (52 * density).toInt()
            )
        }
        cardLayout.addView(registerButton)

        mainLayout.addView(cardLayout)

        // ==========================================
        // FOOTER: Kembali ke Login jika sudah ada akun
        // ==========================================
        val backToLoginTextView = TextView(this).apply {
            text = "Sudah punya akun? Login di sini"
            setTextColor(Color.parseColor("#4A80FF")) // Link biru soft
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, (10 * density).toInt(), 0, (10 * density).toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        mainLayout.addView(backToLoginTextView)

        scrollView.addView(mainLayout)
        setContentView(scrollView)

        // ==========================================
        // LOGIKA AKSI TOMBOL
        // ==========================================
        registerButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim() // Ambil data nomor HP
            val password = passwordEditText.text.toString().trim()

            // Validasi apakah ada field yang kosong termasuk nomor HP
            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Semua data wajib diisi!", Toast.LENGTH_SHORT).show()
            } else if (!termsCheckbox.isChecked) {
                Toast.makeText(this, "Anda harus menyetujui Terms of Service!", Toast.LENGTH_SHORT).show()
            } else {
                // MATIKAN tombol sementara agar user tidak klik berkali-kali saat loading
                registerButton.isEnabled = false
                registerButton.text = "Loading..."

                // Jalankan proses background menggunakan Coroutine
                lifecycleScope.launch {
                    try {
                        // CATATAN: Pastikan key string kiri ("name", "email", dll)
                        // sesuai persis dengan validasi di Controller Laravel Anda.
                        val requestBody = hashMapOf(
                            "full_name" to name,      // Umumnya Laravel menggunakan "name" bukan "full_name" untuk user auth
                            "email" to email,
                            "phone" to phone,    // Mengirim nomor HP ke Laravel
                            "password_hash" to password // Umumnya Laravel menggunakan "password" bukan "password_hash"
                        )

                        val response = ApiClient.instance.registerUser(requestBody)

                        if (response.isSuccessful && response.body() != null) {
                            Toast.makeText(this@RegisterActivity, "Registrasi Berhasil! Silakan Login", Toast.LENGTH_LONG).show()
                            // Lempar user kembali ke halaman Login
                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // Jika Laravel menolak (menampilkan JSON error asli dari server)
                            val errorJson = response.errorBody()?.string()
                            Toast.makeText(this@RegisterActivity, "Laravel Reject: $errorJson", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        // Jika koneksi gagal (misal server mati / IP salah)
                        Toast.makeText(this@RegisterActivity, "Error Koneksi: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        // Hidupkan tombol kembali
                        registerButton.isEnabled = true
                        registerButton.text = "Create account"
                    }
                }
            }
        }

        // Klik teks untuk kembali ke halaman login
        backToLoginTextView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Menutup activity register agar tidak menumpuk di stack back
        }
    }
}