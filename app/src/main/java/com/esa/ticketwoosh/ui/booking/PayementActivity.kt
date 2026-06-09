package com.esa.ticketwoosh.ui.booking

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PaymentActivity : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ambil redirect_url yang dikirim dari halaman sebelumnya
        val paymentUrl = intent.getStringExtra("PAYMENT_URL")

        if (paymentUrl.isNullOrEmpty()) {
            Toast.makeText(this, "URL Pembayaran tidak valid!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Membuat WebView lewat kode programmatic seperti komponen Anda yang lain
        val webView = WebView(this).apply {
            settings.javaScriptEnabled = true // Wajib aktif untuk Midtrans Snap
            settings.domStorageEnabled = true
            
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = request?.url.toString()
                    
                    // Logika mendeteksi status sukses dari redirect Midtrans (jika Anda setup finish redirect di dashboard)
                    if (url.contains("finish") || url.contains("success")) {
                        Toast.makeText(context, "Pembayaran Berhasil / Diproses!", Toast.LENGTH_LONG).show()
                        finish() // Tutup halaman pembayaran
                        return true
                    }
                    return false
                }
            }
        }

        setContentView(webView)
        webView.loadUrl(paymentUrl)
    }
}