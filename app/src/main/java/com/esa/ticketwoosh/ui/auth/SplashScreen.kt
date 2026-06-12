package com.esa.ticketwoosh.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.esa.ticketwoosh.R
import com.esa.ticketwoosh.ui.auth.LoginActivity // Sesuaikan dengan kelas LoginActivity Anda
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {

    private lateinit var contentView: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(androidx.appcompat.R.style.Theme_AppCompat_Light_NoActionBar)
        super.onCreate(savedInstanceState)

        // 1. Membuat Layout Utama secara Programmatic (Tanpa XML)
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            // Mengatur background warna merah dari colors.xml
            setBackgroundColor(resources.getColor(R.color.woosh_red, theme))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        // 2. Membuat Komponen ImageView untuk Logo Woosh
        val logoImageView = ImageView(this).apply {
            // Mengambil gambar logo_woosh yang ada di folder drawable
            setImageResource(R.drawable.logo11)

            // Mengatur ukuran logo di layar (contoh: lebar 250dp, tinggi 250dp)
            val density = resources.displayMetrics.density
            val sizeInDp = (250 * density).toInt()

            layoutParams = LinearLayout.LayoutParams(sizeInDp, sizeInDp).apply {
                gravity = Gravity.CENTER
            }

            // Menjaga rasio logo agar tetap proporsional dan tidak gepeng
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        // 3. Gabungkan Komponen ke dalam Layout
        mainLayout.addView(logoImageView)
        setContentView(mainLayout)

        // 4. Timer Delay Menggunakan Coroutine LifecycleScope (Aman & Modern)
        lifecycleScope.launch {
            delay(3000L) // Menahan layar splash selama 3 detik

            // Berpindah ke Halaman Login setelah 3 detik
            val intent = Intent(this@SplashScreen, RegisterActivity::class.java)
            startActivity(intent)
            finish() // Menutup SplashScreen agar tidak bisa di-back kembali
        }
    }
}