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
                private fun handleUrlRedirect(url: String): Boolean {
                    // Cek jika URL adalah halaman finish Midtrans atau mengandung order_id / transaction_status
                    // Seringkali Midtrans redirect ke example.com jika belum disetting di dashboard
                    if (url.contains("finish") || url.contains("success") || 
                        url.contains("example.com") || url.contains("order_id=") || url.contains("transaction_status=")) {
                        
                        Toast.makeText(context, "Pembayaran Berhasil / Diproses!", Toast.LENGTH_LONG).show()
                        
                        val ticketIntent = android.content.Intent(this@PaymentActivity, TicketActivity::class.java)
                        
                        // Coba ekstrak order_id dari URL kembalian Midtrans jika ada
                        val uri = try { android.net.Uri.parse(url) } catch (e: Exception) { null }
                        val orderId = uri?.getQueryParameter("order_id") ?: intent.getStringExtra("booking_id") ?: "WOOSH-PAID"
                        
                        // Teruskan data dari SeatActivity
                        ticketIntent.putExtra("order_id", orderId)
                        ticketIntent.putExtra("departure_station", intent.getStringExtra("departure_station"))
                        ticketIntent.putExtra("arrival_station", intent.getStringExtra("arrival_station"))
                        ticketIntent.putExtra("date_display", intent.getStringExtra("date_display"))
                        ticketIntent.putExtra("departure_time", intent.getStringExtra("departure_time"))
                        ticketIntent.putExtra("arrival_time", intent.getStringExtra("arrival_time"))
                        ticketIntent.putExtra("train_code", intent.getStringExtra("train_code"))
                        ticketIntent.putExtra("train_class", intent.getStringExtra("train_class"))
                        ticketIntent.putExtra("seat_number", intent.getStringExtra("seat_number"))
                        ticketIntent.putExtra("passenger_name", intent.getStringExtra("passenger_name"))
                        
                        startActivity(ticketIntent)
                        finish() // Tutup halaman pembayaran
                        return true
                    }
                    return false
                }

                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = request?.url.toString()
                    return handleUrlRedirect(url)
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    if (url != null) {
                        handleUrlRedirect(url)
                    }
                }
            }
        }

        setContentView(webView)
        webView.loadUrl(paymentUrl)
    }
}