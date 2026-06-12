package com.esa.ticketwoosh.ui.booking

import android.app.AlertDialog
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
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import com.esa.ticketwoosh.data.api.ApiClient
import com.esa.ticketwoosh.data.model.CheckoutPassenger
import com.esa.ticketwoosh.data.model.CheckoutRequest
import com.esa.ticketwoosh.data.model.SeatApiItem
import com.esa.ticketwoosh.utils.SessionManager
import kotlinx.coroutines.launch

class SeatActivity : AppCompatActivity() {

    // Intent extras
    private var scheduleId: Int = -1
    private var trainId: Int = -1
    private var passengerCount: Int = 1
    private var trainCode: String = ""
    private var trainClass: String = ""
    private var departureTime: String = ""
    private var arrivalTime: String = ""
    private var departureStation: String = ""
    private var arrivalStation: String = ""
    private var price: String = ""
    private var dateDisplay: String = ""
    private var passengerNames = ArrayList<String>()
    private var passengerIds = ArrayList<String>()
    private var passengerTypes = ArrayList<String>()

    // State
    private val selectedSeats = mutableListOf<SeatApiItem>()
    private val seatViews = HashMap<String, Pair<FrameLayout, TextView>>()
    private var activeCarName = "Car 1"

    // Colors
    private val wooshRed = Color.parseColor("#ED1C24")
    private val colorTextPrimary = Color.parseColor("#111827")
    private val colorTextSecondary = Color.parseColor("#6B7280")
    private val colorBorder = Color.parseColor("#E5E7EB")
    private val colorOccupiedBg = Color.parseColor("#E5E7EB")
    private val colorOccupiedText = Color.parseColor("#9CA3AF")
    private val colorSelectedBg = Color.parseColor("#ED1C24")
    private val colorSelectedText = Color.WHITE
    private val colorAvailableBg = Color.WHITE

    // Views
    private lateinit var carSubtitleTextView: TextView
    private lateinit var carriageContainer: LinearLayout
    private lateinit var selectedSeatsSummaryTextView: TextView
    private lateinit var confirmButton: Button
    private lateinit var loadingOverlay: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Read extras
        scheduleId = intent.getIntExtra("SCHEDULE_ID", -1)
        trainId = intent.getIntExtra("TRAIN_ID", -1)
        passengerCount = intent.getIntExtra("PASSENGER_COUNT", 1)
        trainCode = intent.getStringExtra("TRAIN_CODE") ?: ""
        trainClass = intent.getStringExtra("TRAIN_CLASS") ?: ""
        departureTime = intent.getStringExtra("DEPARTURE_TIME") ?: ""
        arrivalTime = intent.getStringExtra("ARRIVAL_TIME") ?: ""
        departureStation = intent.getStringExtra("DEPARTURE_STATION") ?: ""
        arrivalStation = intent.getStringExtra("ARRIVAL_STATION") ?: ""
        price = intent.getStringExtra("PRICE") ?: ""
        dateDisplay = intent.getStringExtra("DATE_DISPLAY") ?: ""
        passengerNames = intent.getStringArrayListExtra("PASSENGER_NAMES") ?: ArrayList()
        passengerIds = intent.getStringArrayListExtra("PASSENGER_IDS") ?: ArrayList()
        passengerTypes = intent.getStringArrayListExtra("PASSENGER_TYPES") ?: ArrayList()

        val d = resources.displayMetrics.density

        // --- ROOT LAYOUT ---
        val root = RelativeLayout(this).apply {
            setBackgroundColor(Color.parseColor("#F9FAFB"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // --- 1. HEADER TOOLBAR ---
        val toolbar = LinearLayout(this).apply {
            id = View.generateViewId()
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(Color.WHITE)
            elevation = 4f * d
            setPadding((16 * d).toInt(), (12 * d).toInt(), (16 * d).toInt(), (12 * d).toInt())
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.ALIGN_PARENT_TOP)
            }
        }

        val backButton = TextView(this).apply {
            text = "\u2190" // Left arrow character
            textSize = 24f
            setTypeface(null, Typeface.BOLD)
            setTextColor(colorTextPrimary)
            gravity = Gravity.CENTER
            val padding = (8 * d).toInt()
            setPadding(padding, padding, padding, padding)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
            }
            setOnClickListener { finish() }
        }
        toolbar.addView(backButton)

        val headerTitles = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                leftMargin = (16 * d).toInt()
            }
        }

        val titleTv = TextView(this).apply {
            text = "Select Seat"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(colorTextPrimary)
        }
        carSubtitleTextView = TextView(this).apply {
            text = "Carriage"
            textSize = 12f
            setTextColor(colorTextSecondary)
        }
        headerTitles.addView(titleTv)
        headerTitles.addView(carSubtitleTextView)
        toolbar.addView(headerTitles)
        root.addView(toolbar)

        // --- 2. BOTTOM STICKY FOOTER ---
        val footer = LinearLayout(this).apply {
            id = View.generateViewId()
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(Color.WHITE)
            elevation = 16f * d
            setPadding((16 * d).toInt(), (16 * d).toInt(), (16 * d).toInt(), (16 * d).toInt())
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            }
        }

        val footerSummary = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.2f)
        }

        val footerLabel = TextView(this).apply {
            text = "Selected Seat"
            textSize = 12f
            setTextColor(colorTextSecondary)
        }
        selectedSeatsSummaryTextView = TextView(this).apply {
            text = "-"
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(colorTextPrimary)
        }
        footerSummary.addView(footerLabel)
        footerSummary.addView(selectedSeatsSummaryTextView)
        footer.addView(footerSummary)

        confirmButton = Button(this).apply {
            text = "Confirm"
            isAllCaps = false
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                setColor(wooshRed)
                cornerRadius = 12f * d
            }
            layoutParams = LinearLayout.LayoutParams(0, (48 * d).toInt(), 0.8f)
            setOnClickListener { onConfirmClicked() }
        }
        footer.addView(confirmButton)
        root.addView(footer)

        // --- 3. SCROLLABLE CONTAINER ---
        val scrollView = NestedScrollView(this).apply {
            // Removed isFillViewport = true to prevent layout measurement bugs
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                addRule(RelativeLayout.BELOW, toolbar.id)
                addRule(RelativeLayout.ABOVE, footer.id)
            }
        }

        val scrollContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((24 * d).toInt(), (20 * d).toInt(), (24 * d).toInt(), (24 * d).toInt())
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // --- Legend Rows ---
        val legendRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = (24 * d).toInt()
            }
        }

        fun createLegendItem(colorBg: Int, textLabel: String): LinearLayout {
            return LinearLayout(this@SeatActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    rightMargin = (16 * d).toInt()
                }

                val colorBox = View(this@SeatActivity).apply {
                    background = GradientDrawable().apply {
                        setColor(colorBg)
                        cornerRadius = 4f * d
                        if (colorBg == Color.WHITE) {
                            setStroke((1 * d).toInt(), colorBorder)
                        }
                    }
                    layoutParams = LinearLayout.LayoutParams((18 * d).toInt(), (18 * d).toInt())
                }

                val label = TextView(this@SeatActivity).apply {
                    text = textLabel
                    textSize = 12f
                    setTextColor(colorTextSecondary)
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        leftMargin = (6 * d).toInt()
                    }
                }

                addView(colorBox)
                addView(label)
            }
        }

        legendRow.addView(createLegendItem(colorAvailableBg, "Available"))
        legendRow.addView(createLegendItem(colorOccupiedBg, "Occupied"))
        legendRow.addView(createLegendItem(colorSelectedBg, "Selected"))
        scrollContent.addView(legendRow)

        // --- Carriage Board Layout (train pod shape) ---
        val carriageBoard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 24f * d
                setStroke((1.5f * d).toInt(), Color.parseColor("#D1D5DB"))
            }
            setPadding((16 * d).toInt(), (24 * d).toInt(), (16 * d).toInt(), (24 * d).toInt())
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Column Labels row: A, B, [Aisle], C, D
        val colHeaderRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = (16 * d).toInt()
            }
        }

        fun createColHeader(letter: String, isSpacer: Boolean = false): View {
            if (isSpacer) {
                return View(this@SeatActivity).apply {
                    layoutParams = LinearLayout.LayoutParams((32 * d).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
                }
            }
            return TextView(this@SeatActivity).apply {
                text = letter
                textSize = 12f
                setTextColor(colorTextSecondary)
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams((44 * d).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
            }
        }

        colHeaderRow.addView(createColHeader("A"))
        colHeaderRow.addView(createColHeader("B"))
        colHeaderRow.addView(createColHeader("", true)) // Aisle space
        colHeaderRow.addView(createColHeader("C"))
        colHeaderRow.addView(createColHeader("D"))
        carriageBoard.addView(colHeaderRow)

        // Carriage grid container where rows will be loaded dynamically
        carriageContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        carriageBoard.addView(carriageContainer)
        scrollContent.addView(carriageBoard)
        scrollView.addView(scrollContent)
        root.addView(scrollView)

        // --- 4. LOADING OVERLAY SPINNING WHEEL ---
        loadingOverlay = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#CCFFFFFF"))
            visibility = View.GONE
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            val pBar = ProgressBar(this@SeatActivity).apply {
                val params = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { gravity = Gravity.CENTER }
                layoutParams = params
            }
            addView(pBar)
        }
        root.addView(loadingOverlay)

        setContentView(root)

        // Fetch seat list from API
        fetchSeats()
    }

    private fun fetchSeats() {
        loadingOverlay.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = ApiClient.instance.getSeats(trainId, scheduleId)
                if (response.isSuccessful && response.body() != null) {
                    val seats = response.body()!!.seats
                    // Filter seats matching current class of interest
                    val targetClass = mapTrainClassToSeatClass(trainClass)
                    val classSeats = seats.filter { it.seatClass.lowercase() == targetClass }

                    if (classSeats.isEmpty()) {
                        generateBackupSeatsAndRender()
                    } else {
                        loadingOverlay.visibility = View.GONE
                        renderSeatGrid(classSeats)
                    }
                } else {
                    generateBackupSeatsAndRender()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                generateBackupSeatsAndRender()
            }
        }
    }

    private fun generateBackupSeatsAndRender() {
        loadingOverlay.visibility = View.GONE
        Toast.makeText(this, "Menggunakan data kursi cadangan (Offline Mode)", Toast.LENGTH_SHORT).show()

        val targetClass = mapTrainClassToSeatClass(trainClass)
        // Map G1, G2, G3 based on targetClass
        val carPrefix = when (targetClass) {
            "vip" -> "G1"
            "business" -> "G2"
            else -> "G3"
        }

        // Generate 40 seats: 1A to 10D
        val backupSeats = mutableListOf<SeatApiItem>()
        var countId = 1
        val columns = listOf("A", "B", "C", "D")
        
        // Some indices pre-booked to simulate occupied seats
        val occupiedCombinations = setOf("2B", "3A", "3C", "3D", "5D", "6B", "6C", "8B", "9A", "10D")

        for (row in 1..10) {
            for (col in columns) {
                val seatNum = "$carPrefix-$row$col"
                val isBooked = occupiedCombinations.contains("$row$col")
                backupSeats.add(
                    SeatApiItem(
                        seatId = countId++,
                        trainId = trainId,
                        seatNumber = seatNum,
                        seatClass = targetClass,
                        isBooked = isBooked
                    )
                )
            }
        }
        renderSeatGrid(backupSeats)
    }

    private fun mapTrainClassToSeatClass(cls: String): String {
        val lower = cls.lowercase()
        return when {
            lower.contains("first") || lower.contains("vip") -> "vip"
            lower.contains("business") || lower.contains("bisnis") -> "business"
            else -> "economy"
        }
    }

    private fun parseSeatNumber(seatNumber: String): Pair<Int, String>? {
        try {
            val parts = seatNumber.trim().split("-")
            val rowCol = parts.last().trim()
            if (rowCol.length < 2) return null
            val col = rowCol.takeLast(1).uppercase()
            val rowStr = rowCol.dropLast(1).trim()
            val row = rowStr.toIntOrNull() ?: return null
            return Pair(row, col)
        } catch (e: Exception) {
            return null
        }
    }

    private fun renderSeatGrid(seats: List<SeatApiItem>) {
        carriageContainer.removeAllViews()
        seatViews.clear()

        val d = resources.displayMetrics.density

        // Determine carriage name from prefix of first seat (e.g. "G1-1A" -> "G1" -> "Car 1")
        if (seats.isNotEmpty()) {
            val parts = seats[0].seatNumber.split("-")
            activeCarName = if (parts.size > 1) {
                val num = parts[0].replace(Regex("[^0-9]"), "")
                if (num.isNotEmpty()) "Car $num" else parts[0]
            } else {
                "Car 1"
            }
            carSubtitleTextView.text = "$activeCarName - ${trainClass.uppercase()}"
        }

        // Map seats by row & col
        val seatMap = HashMap<String, SeatApiItem>()
        for (seat in seats) {
            val parsed = parseSeatNumber(seat.seatNumber)
            if (parsed != null) {
                val (row, col) = parsed
                seatMap["$row$col"] = seat
            }
        }

        // Generate rows 1 to 10
        for (row in 1..10) {
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = (12 * d).toInt()
                }
            }

            // Columns A, B
            rowLayout.addView(buildSeatView(row, "A", seatMap, d))
            rowLayout.addView(buildSeatView(row, "B", seatMap, d))

            // Row Number (middle aisle text)
            val aisleTextView = TextView(this).apply {
                text = row.toString()
                textSize = 14f
                setTextColor(colorTextSecondary)
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams((32 * d).toInt(), (44 * d).toInt())
            }
            rowLayout.addView(aisleTextView)

            // Columns C, D
            rowLayout.addView(buildSeatView(row, "C", seatMap, d))
            rowLayout.addView(buildSeatView(row, "D", seatMap, d))

            carriageContainer.addView(rowLayout)
        }
    }

    private fun buildSeatView(row: Int, col: String, seatMap: Map<String, SeatApiItem>, d: Float): View {
        val key = "$row$col"
        val seat = seatMap[key]

        val container = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams((44 * d).toInt(), (44 * d).toInt()).apply {
                setMargins((4 * d).toInt(), 0, (4 * d).toInt(), 0)
            }
        }

        if (seat == null) {
            // Invisible/blank placeholder if seat doesn't exist
            container.visibility = View.INVISIBLE
            // BUT we still need to return it so spacing is correct
        }

        val textVal = seat?.seatNumber?.split("-")?.last() ?: "$row$col"

        val seatBtnText = TextView(this).apply {
            text = textVal
            textSize = 12f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val bgDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 10f * d
        }

        if (seat != null) {
            if (seat.isBooked) {
                bgDrawable.setColor(colorOccupiedBg)
                seatBtnText.setTextColor(colorOccupiedText)
                container.isEnabled = false
            } else {
                bgDrawable.setColor(colorAvailableBg)
                bgDrawable.setStroke((1 * d).toInt(), colorBorder)
                seatBtnText.setTextColor(colorTextPrimary)
                container.isEnabled = true

                container.setOnClickListener {
                    onSeatClicked(seat, container, seatBtnText)
                }
            }
            container.background = bgDrawable
            container.addView(seatBtnText)
            seatViews[key] = Pair(container, seatBtnText)
        }

        return container
    }

    private fun onSeatClicked(seat: SeatApiItem, layout: FrameLayout, tv: TextView) {
        val d = resources.displayMetrics.density
        val parsed = parseSeatNumber(seat.seatNumber) ?: return
        val key = "${parsed.first}${parsed.second}"

        if (selectedSeats.any { it.seatId == seat.seatId }) {
            // Deselect seat
            selectedSeats.removeAll { it.seatId == seat.seatId }
            layout.background = GradientDrawable().apply {
                setColor(colorAvailableBg)
                cornerRadius = 10f * d
                setStroke((1 * d).toInt(), colorBorder)
            }
            tv.setTextColor(colorTextPrimary)
        } else {
            // Select seat
            if (selectedSeats.size >= passengerCount) {
                // Smooth rotation: Deselect first chosen seat to accommodate new one
                val oldestSeat = selectedSeats.removeAt(0)
                val oldestParsed = parseSeatNumber(oldestSeat.seatNumber)
                if (oldestParsed != null) {
                    val oldestKey = "${oldestParsed.first}${oldestParsed.second}"
                    val oldestViewPair = seatViews[oldestKey]
                    if (oldestViewPair != null) {
                        oldestViewPair.first.background = GradientDrawable().apply {
                            setColor(colorAvailableBg)
                            cornerRadius = 10f * d
                            setStroke((1 * d).toInt(), colorBorder)
                        }
                        oldestViewPair.second.setTextColor(colorTextPrimary)
                    }
                }
            }

            selectedSeats.add(seat)
            layout.background = GradientDrawable().apply {
                setColor(colorSelectedBg)
                cornerRadius = 10f * d
            }
            tv.setTextColor(colorSelectedText)
        }

        // Update footer text
        updateFooterSummary()
    }

    private fun updateFooterSummary() {
        if (selectedSeats.isEmpty()) {
            selectedSeatsSummaryTextView.text = "-"
        } else {
            // Sort selected seats by seat number for nice layout display
            val listText = selectedSeats
                .map { it.seatNumber.split("-").last() }
                .sorted()
                .joinToString(", ")
            selectedSeatsSummaryTextView.text = "$activeCarName, $listText"
        }
    }

    private fun onConfirmClicked() {
        // 1. Validasi pastikan user sudah memilih kursi sesuai jumlah penumpang
        if (selectedSeats.size < passengerCount) {
            Toast.makeText(this, "Harap pilih $passengerCount kursi sesuai jumlah penumpang!", Toast.LENGTH_SHORT).show()
            return
        }

        // Tampilkan loading overlay saat memproses token Midtrans
        loadingOverlay.visibility = View.VISIBLE 

        lifecycleScope.launch {
            try {
                // Mengambil kode nomor kursi saja (misal dari "G1-1A" menjadi "1A"), lalu digabung dengan koma jika lebih dari 1 kursi
                val seatsString = selectedSeats.map { it.seatNumber.split("-").last() }.joinToString(",") 
                
                // Membersihkan string price (misal "Rp 150.000" diambil angka murninya saja 150000)
                val cleanPrice = price.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 150000
                // Hitung total harga dinamis berdasarkan jumlah kursi yang dipilih
                val hitungTotalHarga = selectedSeats.size * cleanPrice 

                val token = SessionManager(this@SeatActivity).fetchAuthToken()
                if (token == null) {
                    loadingOverlay.visibility = View.GONE
                    Toast.makeText(this@SeatActivity, "Sesi login tidak valid. Silakan login kembali.", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val formattedToken = if (token.startsWith("Bearer ")) token else "Bearer $token"

                // 2. Buat data booking dan penumpang terlebih dahulu ke database
                val checkoutPassengers = mutableListOf<CheckoutPassenger>()
                for ((index, seat) in selectedSeats.withIndex()) {
                    val pName = if (index < passengerNames.size) passengerNames[index] else "Penumpang ${index + 1}"
                    val pId = if (index < passengerIds.size) passengerIds[index] else "ID-${index + 1}"
                    checkoutPassengers.add(
                        CheckoutPassenger(
                            fullName = pName,
                            idNumber = pId,
                            seatId = seat.seatId
                        )
                    )
                }

                val checkoutRequest = CheckoutRequest(
                    scheduleId = scheduleId,
                    paymentMethod = "bank_transfer",
                    passengers = checkoutPassengers
                )

                val checkoutResponse = ApiClient.instance.checkout(formattedToken, checkoutRequest)
                if (!checkoutResponse.isSuccessful || checkoutResponse.body() == null) {
                    loadingOverlay.visibility = View.GONE
                    val errorBody = checkoutResponse.errorBody()?.string() ?: "Gagal menyimpan data booking."
                    Toast.makeText(this@SeatActivity, "Gagal membuat booking: $errorBody", Toast.LENGTH_LONG).show()
                    return@launch
                }
                
                val realBookingId = checkoutResponse.body()!!.bookingId ?: 1

                // 3. Panggil API Laravel yang terhubung ke Midtrans Snap dengan data ASLI
                val paymentRequest = com.esa.ticketwoosh.data.model.PaymentRequest(
                    bookingId = realBookingId,
                    totalPrice = hitungTotalHarga,
                    paymentMethod = "bank_transfer"
                )
                
                val response = ApiClient.instance.checkoutPayment(
                    token = formattedToken,
                    request = paymentRequest
                )

                loadingOverlay.visibility = View.GONE

                if (response.isSuccessful && response.body()?.success == true) {
                    val urlPembayaranMidtrans = response.body()!!.redirectUrl
                    
                    // 3. OPER URL KASIR MIDTRANS KE PAYMENT ACTIVITY (WEBVIEW)
                    val intent = Intent(this@SeatActivity, PaymentActivity::class.java)
                    intent.putExtra("PAYMENT_URL", urlPembayaranMidtrans)
                    
                    // Kirim juga data tiket agar nanti diteruskan ke TicketActivity
                    intent.putExtra("departure_station", departureStation)
                    intent.putExtra("arrival_station", arrivalStation)
                    intent.putExtra("date_display", dateDisplay)
                    intent.putExtra("departure_time", departureTime)
                    intent.putExtra("arrival_time", arrivalTime)
                    intent.putExtra("train_code", trainCode)
                    intent.putExtra("train_class", trainClass)
                    intent.putExtra("seat_number", seatsString)
                    intent.putExtra("passenger_name", if (passengerNames.isNotEmpty()) passengerNames.joinToString(", ") else "Penumpang")
                    
                    startActivity(intent)
                    
                } else if (response.code() == 422) {
                    val errorBody = response.errorBody()?.string() ?: "Data pembayaran tidak valid (422)."
                    
                    // Tampilkan pesan error ke user menggunakan AlertDialog agar lebih jelas
                    AlertDialog.Builder(this@SeatActivity)
                        .setTitle("Validasi Error (422)")
                        .setMessage(errorBody)
                        .setPositiveButton("OK", null)
                        .show()
                        
                } else {
                    Toast.makeText(this@SeatActivity, "Gagal membuat sesi pembayaran. Code: ${response.code()}", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                loadingOverlay.visibility = View.GONE
                Toast.makeText(this@SeatActivity, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun performCheckout(methodKey: String, methodLabel: String) {
        val token = SessionManager(this).fetchAuthToken()
        if (token == null) {
            Toast.makeText(this, "Sesi login tidak valid. Silakan login kembali.", Toast.LENGTH_SHORT).show()
            return
        }

        loadingOverlay.visibility = View.VISIBLE

        // Prepare passengers list with chosen seats
        val checkoutPassengers = mutableListOf<CheckoutPassenger>()
        for ((index, seat) in selectedSeats.withIndex()) {
            val pName = if (index < passengerNames.size) passengerNames[index] else "Penumpang ${index + 1}"
            val pId = if (index < passengerIds.size) passengerIds[index] else "ID-${index + 1}"
            checkoutPassengers.add(
                CheckoutPassenger(
                    fullName = pName,
                    idNumber = pId,
                    seatId = seat.seatId
                )
            )
        }

        val checkoutRequest = CheckoutRequest(
            scheduleId = scheduleId,
            paymentMethod = methodKey,
            passengers = checkoutPassengers
        )

        lifecycleScope.launch {
            try {
                val formattedToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
                val response = ApiClient.instance.checkout(formattedToken, checkoutRequest)

                loadingOverlay.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    val res = response.body()!!
                    showSuccessDialog(res, methodLabel)
                } else {
                    val errMsg = response.errorBody()?.string() ?: "Gagal memproses checkout."
                    AlertDialog.Builder(this@SeatActivity)
                        .setTitle("Gagal Checkout")
                        .setMessage(errMsg)
                        .setPositiveButton("OK", null)
                        .show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                loadingOverlay.visibility = View.GONE
                Toast.makeText(this@SeatActivity, "Terjadi kesalahan jaringan: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showSuccessDialog(res: com.esa.ticketwoosh.data.model.CheckoutResponse, methodLabel: String) {
        val d = resources.displayMetrics.density

        // Custom success layout
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding((24 * d).toInt(), (24 * d).toInt(), (24 * d).toInt(), (24 * d).toInt())
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 16f * d
            }
        }

        // Green success check icon (drawn programmatically)
        val successIcon = TextView(this).apply {
            text = "\u2714" // Tick mark
            textSize = 28f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#10B981")) // green
                shape = GradientDrawable.OVAL
            }
            layoutParams = LinearLayout.LayoutParams((60 * d).toInt(), (60 * d).toInt()).apply {
                bottomMargin = (16 * d).toInt()
            }
        }
        container.addView(successIcon)

        val title = TextView(this).apply {
            text = "Booking Berhasil!"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(colorTextPrimary)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = (16 * d).toInt() }
        }
        container.addView(title)

        // Details block
        val detailsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F3F4F6"))
                cornerRadius = 8f * d
            }
            setPadding((16 * d).toInt(), (16 * d).toInt(), (16 * d).toInt(), (16 * d).toInt())
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = (24 * d).toInt() }
        }

        fun addDetailRow(label: String, valText: String) {
            val row = LinearLayout(this@SeatActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (8 * d).toInt() }
            }
            val lbl = TextView(this@SeatActivity).apply {
                text = label
                textSize = 12f
                setTextColor(colorTextSecondary)
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            val vl = TextView(this@SeatActivity).apply {
                text = valText
                textSize = 12f
                setTypeface(null, Typeface.BOLD)
                setTextColor(colorTextPrimary)
                gravity = Gravity.RIGHT
            }
            row.addView(lbl)
            row.addView(vl)
            detailsLayout.addView(row)
        }

        val formattedPrice = try {
            val num = res.totalBayar ?: 0.0
            "Rp " + String.format("%,.0f", num).replace(",", ".")
        } catch (e: Exception) {
            "Rp ${res.totalBayar ?: 0}"
        }

        addDetailRow("Booking Code", res.bookingCode ?: "-")
        addDetailRow("Metode Bayar", methodLabel)
        addDetailRow("Total Bayar", formattedPrice)
        addDetailRow("Batas Waktu", res.batasWaktu ?: "-")

        container.addView(detailsLayout)

        val homeBtn = Button(this).apply {
            text = "Ke Halaman Utama"
            isAllCaps = false
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                setColor(wooshRed)
                cornerRadius = 10f * d
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (44 * d).toInt()
            )
        }
        container.addView(homeBtn)

        val dialog = AlertDialog.Builder(this)
            .setView(container)
            .setCancelable(false)
            .create()

        homeBtn.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this@SeatActivity, BookingActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            finish()
        }

        dialog.show()
    }
}
