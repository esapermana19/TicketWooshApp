package com.esa.ticketwoosh.ui.booking

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.esa.ticketwoosh.R
import com.esa.ticketwoosh.data.api.ApiClient
import com.esa.ticketwoosh.data.model.PaymentRequest
import com.esa.ticketwoosh.utils.SessionManager
import kotlinx.coroutines.launch
import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

class TicketActivity : AppCompatActivity() {

    // Variabel penampung data dari halaman sebelumnya
    private var orderId: String = "WOOSH-123456"
    private var departureStation: String = "Jakarta Halim"
    private var arrivalStation: String = "Bandung Tegalluar"
    private var dateDisplay: String = "Kamis, 11 Juni 2026"
    private var departureTime: String = "08:00"
    private var arrivalTime: String = "08:45"
    private var trainCode: String = "G1-WOOSH"
    private var trainClass: String = "Premium Economy"
    private var seatNumber: String = "1A, 1B"
    private var passengerName: String = "Esa Putra"
    private var bookingId: Int = 0
    private var status: String = "sukses"
    private var totalAmount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mengambil data kiriman dari intent jika tersedia
        intent.getStringExtra("order_id")?.let { orderId = it }
        intent.getStringExtra("departure_station")?.let { departureStation = it }
        intent.getStringExtra("arrival_station")?.let { arrivalStation = it }
        intent.getStringExtra("date_display")?.let { dateDisplay = it }
        intent.getStringExtra("departure_time")?.let { departureTime = it }
        intent.getStringExtra("arrival_time")?.let { arrivalTime = it }
        intent.getStringExtra("train_code")?.let { trainCode = it }
        intent.getStringExtra("train_class")?.let { trainClass = it }
        intent.getStringExtra("seat_number")?.let { seatNumber = it }
        intent.getStringExtra("passenger_name")?.let { passengerName = it }
        bookingId = intent.getIntExtra("booking_id", 0)
        intent.getStringExtra("status")?.let { status = it }
        totalAmount = intent.getIntExtra("total_amount", 0)

        val density = resources.displayMetrics.density
        val wooshRed = resources.getColor(R.color.woosh_red, theme)
        val bgColor = Color.parseColor("#F4F6FA")

        // 1. Root Layout (ScrollView agar aman di layar kecil)
        val scrollView = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(bgColor)
        }

        val mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((20 * density).toInt(), (20 * density).toInt(), (20 * density).toInt(), (30 * density).toInt())
            gravity = Gravity.CENTER_HORIZONTAL
        }

        // Title Halaman
        val pageTitle = TextView(this).apply {
            text = "E-Ticket Anda"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#1C1C1E"))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, (20 * density).toInt())
            }
        }
        mainContainer.addView(pageTitle)

        // =========================================================================
        // KARTU TIKET UTAMA (White Card)
        // =========================================================================
        val ticketCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundDrawable(GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 16 * density
            })
            setPadding((20 * density).toInt(), (20 * density).toInt(), (20 * density).toInt(), (25 * density).toInt())
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Header Tiket: Kode Kereta & Status Badge
        val headerLayout = RelativeLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, (15 * density).toInt())
            }
        }

        val txtTrainCode = TextView(this).apply {
            text = trainCode
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(wooshRed)
            id = View.generateViewId()
        }
        val trainCodeParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            addRule(RelativeLayout.ALIGN_PARENT_LEFT)
            addRule(RelativeLayout.CENTER_VERTICAL)
        }
        headerLayout.addView(txtTrainCode, trainCodeParams)

        // Badge Status "SUCCESS / LUNAS"
        val statusBadge = TextView(this).apply {
            text = status.uppercase()
            textSize = 11f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setPadding((10 * density).toInt(), (4 * density).toInt(), (10 * density).toInt(), (4 * density).toInt())
            val statusColor = when(status.lowercase()) {
                "completed", "paid", "sukses", "success" -> "#28A745"
                "pending" -> "#FFC107"
                "failed", "cancelled" -> "#DC3545"
                else -> "#6C757D"
            }
            setBackgroundDrawable(GradientDrawable().apply {
                setColor(Color.parseColor(statusColor)) 
                cornerRadius = 20 * density
            })
        }
        val badgeParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            addRule(RelativeLayout.CENTER_VERTICAL)
        }
        headerLayout.addView(statusBadge, badgeParams)
        ticketCard.addView(headerLayout)

        // Garis Pembatas Tipis 1
        ticketCard.addView(createDivider(density))

        // Informasi Rute (Stasiun Asal -> Stasiun Tujuan)
        val routeLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 3f
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, (15 * density).toInt(), 0, (15 * density).toInt())
            }
        }

        val originContainer = createStationBlock(this, departureTime, departureStation, Gravity.LEFT)
        val arrowContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            
            // Simbol Tanda Panah Penghubung
            addView(TextView(this@TicketActivity).apply {
                text = "➔"
                textSize = 18f
                setTextColor(Color.parseColor("#8E8E93"))
                gravity = Gravity.CENTER
            })
            addView(TextView(this@TicketActivity).apply {
                text = trainClass
                textSize = 10f
                setTextColor(Color.parseColor("#8E8E93"))
                gravity = Gravity.CENTER
            })
        }
        val destContainer = createStationBlock(this, arrivalTime, arrivalStation, Gravity.RIGHT)

        routeLayout.addView(originContainer)
        routeLayout.addView(arrowContainer)
        routeLayout.addView(destContainer)
        ticketCard.addView(routeLayout)

        // Garis Pembatas Tipis 2
        ticketCard.addView(createDivider(density))

        // Detail Tanggal & Kode Booking
        val detailGrid = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, (15 * density).toInt(), 0, (15 * density).toInt())
            }
        }

        detailGrid.addView(createDetailRow(this, "Tanggal Keberangkatan", dateDisplay))
        detailGrid.addView(createDetailRow(this, "Nama Penumpang", passengerName))
        detailGrid.addView(createDetailRow(this, "Nomor Kursi / Seat", seatNumber))
        detailGrid.addView(createDetailRow(this, "ID Transaksi", orderId))
        ticketCard.addView(detailGrid)

        // Garis Pembatas Sobekan Tiket (Dotted/Garis Putus-Putus)
        val ticketTearLine = View(this).apply {
            setBackgroundDrawable(GradientDrawable().apply {
                setStroke((1 * density).toInt(), Color.parseColor("#C7C7CC"), 4 * density, 4 * density)
            })
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (2 * density).toInt()
            ).apply {
                setMargins(0, (10 * density).toInt(), 0, (20 * density).toInt())
            }
        }
        ticketCard.addView(ticketTearLine)

        // =========================================================================
        // SECTION QR CODE UNTUK BOARDING STATION
        // =========================================================================
        val qrContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Kotak QR Code
        val qrCodeBox = ImageView(this).apply {
            val qrSize = (160 * density).toInt()
            val qrBitmap = generateQRCode(orderId, qrSize)

            if (qrBitmap != null) {
                setImageBitmap(qrBitmap)
                scaleType = ImageView.ScaleType.FIT_CENTER
                setBackgroundColor(Color.WHITE)
            } else {
                // Fallback jika gagal generate
                setBackgroundDrawable(GradientDrawable().apply {
                    setColor(Color.parseColor("#E5E5EA"))
                    setStroke((1 * density).toInt(), Color.parseColor("#C7C7CC"))
                    cornerRadius = 8 * density
                })
                setImageResource(android.R.drawable.ic_menu_crop)
                scaleType = ImageView.ScaleType.CENTER_INSIDE
            }

            layoutParams = LinearLayout.LayoutParams(qrSize, qrSize).apply {
                setMargins(0, 0, 0, (10 * density).toInt())
            }
        }
        qrContainer.addView(qrCodeBox)

        val qrInstruction = TextView(this).apply {
            text = "Pindai QR Code ini di gerbang masuk stasiun saat boarding."
            textSize = 12f
            setTextColor(Color.parseColor("#8E8E93"))
            gravity = Gravity.CENTER
            setPadding((10 * density).toInt(), 0, (10 * density).toInt(), 0)
        }
        qrContainer.addView(qrInstruction)
        ticketCard.addView(qrContainer)

        mainContainer.addView(ticketCard)

        // Tombol Kembali ke Beranda Utama
        val btnBackHome = Button(this).apply {
            text = "Kembali ke Beranda"
            textSize = 15f
            isAllCaps = false
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                setColor(wooshRed)
                cornerRadius = 10 * density
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (48 * density).toInt()
            ).apply {
                setMargins(0, (25 * density).toInt(), 0, 0)
            }
        }
        btnBackHome.setOnClickListener {
            // Tutup halaman tiket dan bersihkan stack agar kembali ke menu utama BookingActivity
            finish()
        }
        mainContainer.addView(btnBackHome)

        // Tombol Bayar Sekarang khusus PENDING
        if (status.lowercase() == "pending") {
            val btnPayNow = Button(this).apply {
                text = "Bayar Sekarang"
                textSize = 15f
                isAllCaps = false
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.WHITE)
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#FFC107"))
                    cornerRadius = 10 * density
                }
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (48 * density).toInt()
                ).apply {
                    setMargins(0, (15 * density).toInt(), 0, 0)
                }
            }
            btnPayNow.setOnClickListener {
                val sessionManager = SessionManager(this@TicketActivity)
                val tokenStr = "Bearer " + (sessionManager.fetchAuthToken() ?: "")
                val req = PaymentRequest(
                    bookingId = bookingId,
                    totalPrice = totalAmount,
                    paymentMethod = "bank_transfer"
                )
                lifecycleScope.launch {
                    try {
                        val res = ApiClient.instance.checkoutPayment(tokenStr, req)
                        if (res.isSuccessful && res.body()?.success == true) {
                            val paymentUrl = res.body()?.redirectUrl
                            val intent = Intent(this@TicketActivity, PaymentActivity::class.java)
                            intent.putExtra("PAYMENT_URL", paymentUrl)
                            intent.putExtra("booking_id", orderId)
                            intent.putExtra("departure_station", departureStation)
                            intent.putExtra("arrival_station", arrivalStation)
                            intent.putExtra("date_display", dateDisplay)
                            intent.putExtra("departure_time", departureTime)
                            intent.putExtra("arrival_time", arrivalTime)
                            intent.putExtra("train_code", trainCode)
                            intent.putExtra("train_class", trainClass)
                            intent.putExtra("seat_number", seatNumber)
                            intent.putExtra("passenger_name", passengerName)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@TicketActivity, "Gagal memproses pembayaran", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@TicketActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            mainContainer.addView(btnPayNow)
        }

        scrollView.addView(mainContainer)
        setContentView(scrollView)
    }

    // Fungsi Pembantu: Membuat Blok Info Stasiun
    private fun createStationBlock(context: AppCompatActivity, time: String, stationName: String, gravityAlign: Int): LinearLayout {
        val density = context.resources.displayMetrics.density
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = gravityAlign
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)

            addView(TextView(context).apply {
                text = time
                textSize = 22f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#1C1C1E"))
                gravity = gravityAlign
            })

            addView(TextView(context).apply {
                text = stationName
                textSize = 13f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#3A3A3C"))
                gravity = gravityAlign
                maxLines = 2
            })
        }
    }

    // Fungsi Pembantu: Membuat Garis Pembatas Biasa
    private fun createDivider(density: Float): View {
        return View(this).apply {
            setBackgroundColor(Color.parseColor("#E5E5EA"))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (1 * density).toInt()
            )
        }
    }

    // Fungsi Pembantu: Membuat Baris Info Detail Tiket (Key - Value)
    private fun createDetailRow(context: AppCompatActivity, label: String, value: String): LinearLayout {
        val density = context.resources.displayMetrics.density
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, (6 * density).toInt(), 0, (6 * density).toInt())
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            // Label Kiri
            addView(TextView(context).apply {
                text = label
                textSize = 13f
                setTextColor(Color.parseColor("#8E8E93"))
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.2f)
            })

            // Isi Value Kanan
            addView(TextView(context).apply {
                text = value
                textSize = 13f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#1C1C1E"))
                gravity = Gravity.RIGHT
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.8f)
            })
        }
    }

    // Fungsi Pembantu: Generate QR Code Menggunakan ZXing
    private fun generateQRCode(text: String, size: Int): Bitmap? {
        try {
            val bitMatrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}