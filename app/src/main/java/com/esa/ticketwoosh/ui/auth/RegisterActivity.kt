package com.esa.ticketwoosh.ui.auth

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.esa.ticketwoosh.R
import com.esa.ticketwoosh.data.api.ApiClient
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

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
            setPadding(padding, (40 * density).toInt(), padding, padding)
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
                bottomMargin = (16 * density).toInt()
            }
        }
        mainLayout.addView(logoImageView)

        // ==========================================
        // 2. Titles
        // ==========================================
        val welcomeTitle = TextView(this).apply {
            text = "Buat Akun Baru"
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
            text = "Daftar untuk mulai perjalanan Anda bersama Woosh"
            textSize = 14f
            setTextColor(textColorSecondary)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = (24 * density).toInt()
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

        fun createInputField(labelText: String, hintText: String, inputTypeEnum: Int): EditText {
            val label = TextView(this@RegisterActivity).apply {
                text = labelText
                textSize = 13f
                setTextColor(textColorSecondary)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (8 * density).toInt() }
            }
            mainLayout.addView(label)

            val editText = EditText(this@RegisterActivity).apply {
                hint = hintText
                textSize = 15f
                setTextColor(Color.BLACK)
                setPadding((16 * density).toInt(), (14 * density).toInt(), (16 * density).toInt(), (14 * density).toInt())
                inputType = inputTypeEnum
                background = getInputBorder()
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (16 * density).toInt() }
            }
            mainLayout.addView(editText)
            return editText
        }

        val nameEditText = createInputField("Nama Lengkap", "Masukkan nama Anda", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PERSON_NAME)
        val emailEditText = createInputField("Email", "contoh@email.com", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        val phoneEditText = createInputField("Nomor Ponsel", "081234567890", InputType.TYPE_CLASS_PHONE)
        val passwordEditText = createInputField("Kata Sandi", "••••••••", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        
        // ==========================================
        // 4. Terms Checkbox
        // ==========================================
        val termsCheckbox = CheckBox(this).apply {
            text = "Saya setuju dengan Syarat & Ketentuan"
            setTextColor(textColorSecondary)
            buttonTintList = android.content.res.ColorStateList.valueOf(wooshRed)
            textSize = 13f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = (24 * density).toInt() }
        }
        mainLayout.addView(termsCheckbox)

        // ==========================================
        // 5. Register Button
        // ==========================================
        val registerButton = Button(this).apply {
            text = "Daftar Sekarang →"
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
        mainLayout.addView(registerButton)

        // Spacer to push login text to bottom
        val spacer = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        mainLayout.addView(spacer)

        // ==========================================
        // 6. Login Text
        // ==========================================
        val backToLoginTextView = TextView(this).apply {
            text = "Sudah punya akun? Masuk di sini"
            setTextColor(textColorSecondary)
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, (20 * density).toInt(), 0, (20 * density).toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (20 * density).toInt()
            }
            setOnClickListener {
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        mainLayout.addView(backToLoginTextView)

        scrollView.addView(mainLayout)
        setContentView(scrollView)

        // ==========================================
        // LOGIC
        // ==========================================
        registerButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Semua data wajib diisi!", Toast.LENGTH_SHORT).show()
            } else if (!termsCheckbox.isChecked) {
                Toast.makeText(this, "Anda harus menyetujui Syarat & Ketentuan!", Toast.LENGTH_SHORT).show()
            } else {
                registerButton.isEnabled = false
                registerButton.text = "Loading..."

                lifecycleScope.launch {
                    try {
                        val requestBody = hashMapOf(
                            "full_name" to name,
                            "email" to email,
                            "phone" to phone,
                            "password_hash" to password
                        )

                        val response = ApiClient.instance.registerUser(requestBody)

                        if (response.isSuccessful && response.body() != null) {
                            Toast.makeText(this@RegisterActivity, "Registrasi Berhasil! Silakan Login", Toast.LENGTH_LONG).show()
                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            val errorJson = response.errorBody()?.string()
                            Toast.makeText(this@RegisterActivity, "Registrasi Gagal: Cek format atau email mungkin sudah terdaftar", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@RegisterActivity, "Error Koneksi: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        registerButton.isEnabled = true
                        registerButton.text = "Daftar Sekarang →"
                    }
                }
            }
        }
    }
}