package com.esa.ticketwoosh.ui.booking

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.esa.ticketwoosh.R
import com.esa.ticketwoosh.data.api.ApiClient
import com.esa.ticketwoosh.data.model.ScheduleItem
import com.esa.ticketwoosh.data.model.StationItem
import com.esa.ticketwoosh.data.model.TrainItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ScheduleActivity : AppCompatActivity() {

    private var originId: Int = 1
    private var destinationId: Int = 4
    private var originName: String = "Halim"
    private var destinationName: String = "Tegalluar"
    private var dateStr: String = ""
    private var dateDisplay: String = ""
    private var passengerCount: Int = 1

    private lateinit var titleTextView: TextView
    private lateinit var subtitleTextView: TextView
    private lateinit var dateStripLayout: LinearLayout
    private lateinit var schedulesContainer: LinearLayout
    private lateinit var progressBar: ProgressBar

    private var currentDate: Calendar = Calendar.getInstance()
    private val wooshRed = Color.parseColor("#E01A22")
    private val textColorPrimary = Color.parseColor("#1F2937")
    private val textColorSecondary = Color.parseColor("#4B5563")
    private val borderColor = Color.parseColor("#E5E7EB")

    /** Card yang sedang dipilih (untuk pindahkan outline merah) */
    private var selectedCardView: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ambil data dari Intent
        originId = intent.getIntExtra("ORIGIN_ID", 1)
        destinationId = intent.getIntExtra("DESTINATION_ID", 4)
        originName = intent.getStringExtra("ORIGIN_NAME") ?: "Halim (Jakarta)"
        destinationName = intent.getStringExtra("DESTINATION_NAME") ?: "Tegalluar (Bandung)"
        dateStr = intent.getStringExtra("DATE_STR") ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        dateDisplay = intent.getStringExtra("DATE_DISPLAY") ?: ""
        passengerCount = intent.getIntExtra("PAX_COUNT", 1)

        // Parse dateStr ke currentDate
        try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateStr)
            if (date != null) {
                currentDate.time = date
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val density = resources.displayMetrics.density

        // --- ROOT LAYOUT ---
        val rootLayout = RelativeLayout(this).apply {
            setBackgroundColor(Color.parseColor("#F9FAFB"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // --- HEADER BAR ---
        val headerBar = RelativeLayout(this).apply {
            id = View.generateViewId()
            setBackgroundColor(Color.WHITE)
            setPadding((16 * density).toInt(), (12 * density).toInt(), (16 * density).toInt(), (12 * density).toInt())
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            // Tambahkan border tipis di bawah header
            val borderDrawable = GradientDrawable().apply {
                setColor(Color.WHITE)
                setStroke((1 * density).toInt(), borderColor)
            }
            background = borderDrawable
        }

        // Tombol Back (<)
        val backButton = TextView(this).apply {
            id = View.generateViewId()
            text = "◀"
            textSize = 18f
            setTextColor(textColorPrimary)
            typeface = Typeface.DEFAULT_BOLD
            setPadding((8 * density).toInt(), (8 * density).toInt(), (8 * density).toInt(), (8 * density).toInt())
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
            setOnClickListener { finish() }
        }
        headerBar.addView(backButton)

        // Judul & Subjudul layout
        val titleLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.RIGHT_OF, backButton.id)
                addRule(RelativeLayout.LEFT_OF, View.generateViewId()) // filter icon placeholder
                leftMargin = (16 * density).toInt()
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
        }

        titleTextView = TextView(this).apply {
            // Bersihkan nama kota untuk tampilan ringkas (misal "Halim (Jakarta)" -> "Halim")
            val cleanOrigin = originName.substringBefore(" (")
            val cleanDest = destinationName.substringBefore(" (")
            text = "$cleanOrigin to $cleanDest"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(textColorPrimary)
        }

        subtitleTextView = TextView(this).apply {
            text = "$dateDisplay • $passengerCount Passenger"
            textSize = 12f
            setTextColor(textColorSecondary)
        }
        titleLayout.addView(titleTextView)
        titleLayout.addView(subtitleTextView)
        headerBar.addView(titleLayout)

        // Filter Icon di kanan
        val filterIcon = ImageView(this).apply {
            id = View.generateViewId()
            setImageResource(android.R.drawable.ic_menu_manage) // Standard icon
            setColorFilter(textColorPrimary)
            layoutParams = RelativeLayout.LayoutParams((24 * density).toInt(), (24 * density).toInt()).apply {
                addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                addRule(RelativeLayout.CENTER_VERTICAL)
                rightMargin = (8 * density).toInt()
            }
        }
        headerBar.addView(filterIcon)
        rootLayout.addView(headerBar)

        // --- DATE STRIP (Horizontal Dates) ---
        val horizontalScrollView = HorizontalScrollView(this).apply {
            id = View.generateViewId()
            setBackgroundColor(Color.WHITE)
            isHorizontalScrollBarEnabled = false
            setPadding(0, (12 * density).toInt(), 0, (12 * density).toInt())
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.BELOW, headerBar.id)
            }
        }

        dateStripLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding((16 * density).toInt(), 0, (16 * density).toInt(), 0)
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        horizontalScrollView.addView(dateStripLayout)
        rootLayout.addView(horizontalScrollView)

        // --- SCHEDULE LIST SCROLLVIEW ---
        val scrollView = ScrollView(this).apply {
            isFillViewport = true
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                addRule(RelativeLayout.BELOW, horizontalScrollView.id)
            }
        }

        schedulesContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((16 * density).toInt(), (16 * density).toInt(), (16 * density).toInt(), (32 * density).toInt())
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        scrollView.addView(schedulesContainer)
        rootLayout.addView(scrollView)

        // --- PROGRESS BAR ---
        progressBar = ProgressBar(this).apply {
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.CENTER_IN_PARENT)
            }
            visibility = View.GONE
        }
        rootLayout.addView(progressBar)

        setContentView(rootLayout)

        // Generate strip tanggal
        rebuildDateStrip()

        // Ambil data jadwal
        fetchSchedules()
    }

    private fun rebuildDateStrip() {
        dateStripLayout.removeAllViews()
        val density = resources.displayMetrics.density

        // Kita tampilkan 5 tanggal: H-2, H-1, H, H+1, H+2
        val dateIterator = currentDate.clone() as Calendar
        dateIterator.add(Calendar.DAY_OF_YEAR, -2)

        for (i in 0 until 5) {
            val itemDate = dateIterator.clone() as Calendar
            val isActive = i == 2 // Hari ke-3 (tengah) adalah tanggal aktif

            val dayName = SimpleDateFormat("dd MMM", Locale.US).format(itemDate.time)

            val dateItem = TextView(this).apply {
                text = dayName
                textSize = 14f
                gravity = Gravity.CENTER
                setPadding((16 * density).toInt(), (10 * density).toInt(), (16 * density).toInt(), (10 * density).toInt())

                val params = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    rightMargin = (8 * density).toInt()
                }
                layoutParams = params

                if (isActive) {
                    setTextColor(Color.WHITE)
                    background = GradientDrawable().apply {
                        setColor(Color.BLACK)
                        cornerRadius = 20f * density
                    }
                } else {
                    setTextColor(textColorPrimary)
                    background = GradientDrawable().apply {
                        setColor(Color.WHITE)
                        setStroke((1 * density).toInt(), borderColor)
                        cornerRadius = 20f * density
                    }
                }

                setOnClickListener {
                    currentDate = itemDate
                    dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(currentDate.time)
                    dateDisplay = SimpleDateFormat("EEE, dd MMM yyyy", Locale.US).format(currentDate.time)
                    subtitleTextView.text = "$dateDisplay • $passengerCount Passenger"

                    rebuildDateStrip()
                    fetchSchedules()
                }
            }

            dateStripLayout.addView(dateItem)
            dateIterator.add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    private fun fetchSchedules() {
        progressBar.visibility = View.VISIBLE
        schedulesContainer.removeAllViews()

        lifecycleScope.launch {
            try {
                // Panggil API Laravel
                val response = ApiClient.instance.searchSchedules(originId, destinationId, dateStr)
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    val scheduleList = response.body()!!.data
                    if (scheduleList.isEmpty()) {
                        showEmptyState()
                    } else {
                        populateScheduleCards(scheduleList)
                    }
                } else {
                    // Jika API mengembalikan 404 (tidak ditemukan), kita buat backup data untuk simulasi
                    generateBackupSchedules()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                progressBar.visibility = View.GONE
                // Jika offline / server mati, gunakan backup data
                generateBackupSchedules()
            }
        }
    }

    private fun showEmptyState() {
        val density = resources.displayMetrics.density
        val emptyText = TextView(this).apply {
            text = "No schedules available for this route and date."
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(textColorSecondary)
            setPadding(0, (40 * density).toInt(), 0, 0)
        }
        schedulesContainer.addView(emptyText)
    }

    private fun populateScheduleCards(items: List<ScheduleItem>) {
        val density = resources.displayMetrics.density

        for ((index, item) in items.withIndex()) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                background = GradientDrawable().apply {
                    setColor(Color.WHITE)
                    cornerRadius = 16f * density
                    setStroke((1 * density).toInt(), borderColor)
                }
                elevation = 4f * density
                setPadding((16 * density).toInt(), (16 * density).toInt(), (16 * density).toInt(), (16 * density).toInt())
                val params = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = (16 * density).toInt()
                }
                layoutParams = params
            }

            // --- HEADER ROW (Train Code, Class, Price) ---
            val headerRow = RelativeLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (12 * density).toInt() }
            }

            // Ikon Kereta bulat merah
            val trainIconContainer = FrameLayout(this).apply {
                id = View.generateViewId()
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#FEE2E2"))
                    shape = GradientDrawable.OVAL
                }
                setPadding((6 * density).toInt(), (6 * density).toInt(), (6 * density).toInt(), (6 * density).toInt())
                layoutParams = RelativeLayout.LayoutParams((32 * density).toInt(), (32 * density).toInt()).apply {
                    addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                    addRule(RelativeLayout.CENTER_VERTICAL)
                }
            }
            val trainIcon = ImageView(this).apply {
                setImageResource(R.drawable.ic_train)
                setColorFilter(wooshRed)
            }
            trainIconContainer.addView(trainIcon)
            headerRow.addView(trainIconContainer)

            // Nama & Kelas Kereta
            val trainInfoLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    addRule(RelativeLayout.RIGHT_OF, trainIconContainer.id)
                    leftMargin = (10 * density).toInt()
                    addRule(RelativeLayout.CENTER_VERTICAL)
                }
            }

            val trainCodeText = TextView(this).apply {
                text = item.train.trainCode
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(textColorPrimary)
            }

            // Tentukan Kelas dinamis berdasarkan ID atau harga
            val priceAmount = item.price.toDoubleOrNull() ?: 0.0
            val className = when {
                priceAmount >= 400000.0 -> "FIRST CLASS"
                priceAmount >= 250000.0 -> "BUSINESS CLASS"
                else -> "PREMIUM ECONOMY"
            }

            val trainClassText = TextView(this).apply {
                text = className
                textSize = 10f
                setTextColor(textColorSecondary)
            }
            trainInfoLayout.addView(trainCodeText)
            trainInfoLayout.addView(trainClassText)
            headerRow.addView(trainInfoLayout)

            // Harga di kanan
            val priceText = TextView(this).apply {
                text = formatPrice(item.price)
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(wooshRed)
                layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                    addRule(RelativeLayout.CENTER_VERTICAL)
                }
            }
            headerRow.addView(priceText)
            card.addView(headerRow)

            // --- TIMELINE ROW (Departure Time, Line, Arrival Time) ---
            val timelineRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { 
                    topMargin = (8 * density).toInt()
                    bottomMargin = (12 * density).toInt() 
                }
            }

            // Asal
            val departureLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            val depTimeText = TextView(this).apply {
                text = item.departureTime.substringAfter(" ").substringBeforeLast(":")
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(textColorPrimary)
            }
            val depCodeText = TextView(this).apply {
                text = item.departureStation.code
                textSize = 11f
                setTextColor(textColorSecondary)
            }
            departureLayout.addView(depTimeText)
            departureLayout.addView(depCodeText)
            timelineRow.addView(departureLayout)

            // Garis Tengah (Direct Line)
            val middleLineLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2f)
            }
            val durationText = TextView(this).apply {
                text = "45m"
                textSize = 10f
                setTextColor(textColorSecondary)
            }
            // Gambar garis dengan titik ujung
            val lineView = View(this).apply {
                background = GradientDrawable().apply {
                    setColor(borderColor)
                }
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (2 * density).toInt()
                ).apply {
                    topMargin = (4 * density).toInt()
                    bottomMargin = (4 * density).toInt()
                }
            }
            val directText = TextView(this).apply {
                text = "Direct"
                textSize = 10f
                setTextColor(textColorSecondary)
            }
            middleLineLayout.addView(durationText)
            middleLineLayout.addView(lineView)
            middleLineLayout.addView(directText)
            timelineRow.addView(middleLineLayout)

            // Tujuan
            val arrivalLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            val arrTimeText = TextView(this).apply {
                text = item.arrivalTime.substringAfter(" ").substringBeforeLast(":")
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(textColorPrimary)
            }
            val arrCodeText = TextView(this).apply {
                text = item.arrivalStation.code
                textSize = 11f
                setTextColor(textColorSecondary)
            }
            arrivalLayout.addView(arrTimeText)
            arrivalLayout.addView(arrCodeText)
            timelineRow.addView(arrivalLayout)

            card.addView(timelineRow)

            // --- FOOTER ROW (Seats Left & Select Button) ---
            val footerRow = RelativeLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = (8 * density).toInt() }
            }

            // --- Ketersediaan kursi dari data API (available_seats) ---
            // null = field belum ada di response API (Gson tidak set default Kotlin)
            val seats = item.availableSeats
            val (statusText, statusColor, isSoldOut) = when {
                seats == null                 -> Triple("Available", Color.parseColor("#10B981"), false)
                seats == 0                    -> Triple("Sold Out", Color.parseColor("#EF4444"), true)
                seats in 1..5                 -> Triple("$seats seats left", Color.parseColor("#F59E0B"), false)
                seats in 6..20               -> Triple("$seats seats left", Color.parseColor("#10B981"), false)
                else /* > 20 */               -> Triple("$seats seats left", Color.parseColor("#10B981"), false)
            }

            val seatStatusText = TextView(this).apply {
                text = statusText
                textSize = 13f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(statusColor)
                layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                    addRule(RelativeLayout.CENTER_VERTICAL)
                }
            }
            footerRow.addView(seatStatusText)

            // Tombol Select (hanya muncul jika tidak sold out)
            if (!isSoldOut) {
                val selectBtn = TextView(this).apply {
                    text = "Select"
                    textSize = 12f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(Color.WHITE)
                    gravity = Gravity.CENTER
                    background = GradientDrawable().apply {
                        setColor(Color.BLACK)
                        cornerRadius = 15f * density
                    }
                    setPadding((20 * density).toInt(), (6 * density).toInt(), (20 * density).toInt(), (6 * density).toInt())
                    layoutParams = RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                        addRule(RelativeLayout.CENTER_VERTICAL)
                    }
                    setOnClickListener {
                        // Pindahkan outline merah ke card yang dipilih
                        selectedCardView?.background = GradientDrawable().apply {
                            setColor(Color.WHITE)
                            cornerRadius = 16f * density
                            setStroke((1 * density).toInt(), borderColor)
                        }
                        card.background = GradientDrawable().apply {
                            setColor(Color.WHITE)
                            cornerRadius = 16f * density
                            setStroke((2 * density).toInt(), wooshRed)
                        }
                        selectedCardView = card

                        // Launch CheckoutActivity dengan detail jadwal lengkap
                        val checkoutIntent = Intent(this@ScheduleActivity, CheckoutActivity::class.java).apply {
                            putExtra("SCHEDULE_ID", item.scheduleId)
                            putExtra("TRAIN_ID", item.trainId)
                            putExtra("PASSENGER_COUNT", passengerCount)
                            putExtra("TRAIN_CODE", item.train.trainCode)
                            putExtra("TRAIN_CLASS", className)
                            putExtra("DEPARTURE_TIME", item.departureTime.substringAfter(" ").substringBeforeLast(":"))
                            putExtra("ARRIVAL_TIME", item.arrivalTime.substringAfter(" ").substringBeforeLast(":"))
                            putExtra("DEPARTURE_STATION", item.departureStation.code)
                            putExtra("ARRIVAL_STATION", item.arrivalStation.code)
                            putExtra("PRICE", item.price)
                            putExtra("DATE_DISPLAY", dateDisplay)
                        }
                        startActivity(checkoutIntent)
                    }
                }
                footerRow.addView(selectBtn)
            }
            card.addView(footerRow)

            schedulesContainer.addView(card)
        }
    }

    private fun generateBackupSchedules() {
        val cleanOrigin = originName.substringBefore(" (")
        val cleanDest = destinationName.substringBefore(" (")
        
        val originCode = when (cleanOrigin) {
            "Halim" -> "HLM"
            "Karawang" -> "KWG"
            "Padalarang" -> "PDL"
            "Tegalluar" -> "TGL"
            else -> "HLM"
        }
        val destCode = when (cleanDest) {
            "Halim" -> "HLM"
            "Karawang" -> "KWG"
            "Padalarang" -> "PDL"
            "Tegalluar" -> "TGL"
            else -> "PDL"
        }

        // Backup: available_seats diisi secara eksplisit agar tidak tampil dummy
        val backupList = listOf(
            ScheduleItem(
                scheduleId = 1,
                trainId = 1,
                departureStation = StationItem(originId, cleanOrigin, cleanOrigin, originCode),
                arrivalStation = StationItem(destinationId, cleanDest, cleanDest, destCode),
                departureTime = "$dateStr 08:45:00",
                arrivalTime = "$dateStr 09:30:00",
                price = "250000.00",
                train = TrainItem(1, "Woosh Train 1", "G7701", 600),
                availableSeats = 0   // Sold Out
            ),
            ScheduleItem(
                scheduleId = 2,
                trainId = 2,
                departureStation = StationItem(originId, cleanOrigin, cleanOrigin, originCode),
                arrivalStation = StationItem(destinationId, cleanDest, cleanDest, destCode),
                departureTime = "$dateStr 09:45:00",
                arrivalTime = "$dateStr 10:30:00",
                price = "300000.00",
                train = TrainItem(2, "Woosh Train 2", "G7703", 600),
                availableSeats = 12
            ),
            ScheduleItem(
                scheduleId = 3,
                trainId = 3,
                departureStation = StationItem(originId, cleanOrigin, cleanOrigin, originCode),
                arrivalStation = StationItem(destinationId, cleanDest, cleanDest, destCode),
                departureTime = "$dateStr 10:45:00",
                arrivalTime = "$dateStr 11:30:00",
                price = "600000.00",
                train = TrainItem(3, "Woosh Train 3", "G7705", 600),
                availableSeats = 5
            ),
            ScheduleItem(
                scheduleId = 4,
                trainId = 4,
                departureStation = StationItem(originId, cleanOrigin, cleanOrigin, originCode),
                arrivalStation = StationItem(destinationId, cleanDest, cleanDest, destCode),
                departureTime = "$dateStr 13:00:00",
                arrivalTime = "$dateStr 13:45:00",
                price = "250000.00",
                train = TrainItem(4, "Woosh Train 4", "G7707", 600),
                availableSeats = 2
            )
        )

        progressBar.visibility = View.GONE
        populateScheduleCards(backupList)
    }

    private fun formatPrice(priceStr: String): String {
        val amount = priceStr.toDoubleOrNull() ?: 0.0
        val formatter = java.text.NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        formatter.maximumFractionDigits = 0
        return formatter.format(amount).replace("Rp", "Rp ")
    }
}
