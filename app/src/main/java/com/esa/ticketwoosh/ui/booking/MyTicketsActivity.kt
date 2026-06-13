package com.esa.ticketwoosh.ui.booking

import android.content.Intent
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.esa.ticketwoosh.R
import com.esa.ticketwoosh.data.api.ApiClient
import com.esa.ticketwoosh.data.model.TicketHistoryItem
import com.esa.ticketwoosh.utils.BottomNavigationUtil
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MyTicketsActivity : AppCompatActivity() {

    private lateinit var ticketsContainer: LinearLayout
    private lateinit var upcomingTab: TextView
    private lateinit var pastTab: TextView
    private lateinit var progressBar: ProgressBar

    private var token: String = ""
    private var allTickets: List<TicketHistoryItem> = emptyList()
    private var isShowingUpcoming = true // True for Upcoming, False for Past

    // Colors
    private val wooshRed = Color.parseColor("#ED1C24")
    private val darkRed = Color.parseColor("#C6181F")
    private val bgColor = Color.parseColor("#F4F6FA")
    private val textColorPrimary = Color.parseColor("#1C1C1E")
    private val textColorSecondary = Color.parseColor("#6B7280")
    private val borderColor = Color.parseColor("#E5E7EB")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = com.esa.ticketwoosh.utils.SessionManager(this)
        val savedToken = sessionManager.fetchAuthToken() ?: ""
        token = "Bearer $savedToken"

        val density = resources.displayMetrics.density

        val rootLayout = RelativeLayout(this).apply {
            setBackgroundColor(bgColor)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Bottom Navigation
        val bottomNavId = BottomNavigationUtil.setupBottomNav(this, rootLayout, 1)

        val mainContent = RelativeLayout(this).apply {
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                addRule(RelativeLayout.ABOVE, bottomNavId)
            }
        }

        // --- HEADER RED BACKGROUND ---
        val headerLayout = LinearLayout(this).apply {
            id = View.generateViewId()
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(wooshRed)
                // Rounded bottom corners
                cornerRadii = floatArrayOf(
                    0f, 0f, 0f, 0f,
                    32f * density, 32f * density,
                    32f * density, 32f * density
                )
            }
            setPadding((24 * density).toInt(), (32 * density).toInt(), (24 * density).toInt(), (24 * density).toInt())
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val titleText = TextView(this).apply {
            text = "My Tickets"
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = (20 * density).toInt()
            }
        }
        headerLayout.addView(titleText)

        // Tab Bar Container
        val tabBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            background = GradientDrawable().apply {
                setColor(darkRed)
                cornerRadius = 24f * density
            }
            setPadding((4 * density).toInt(), (4 * density).toInt(), (4 * density).toInt(), (4 * density).toInt())
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (48 * density).toInt()
            )
        }

        upcomingTab = TextView(this).apply {
            text = "Upcoming"
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
            setOnClickListener {
                isShowingUpcoming = true
                updateTabUI()
                renderTickets()
            }
        }

        pastTab = TextView(this).apply {
            text = "Past Tickets"
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
            setOnClickListener {
                isShowingUpcoming = false
                updateTabUI()
                renderTickets()
            }
        }

        tabBar.addView(upcomingTab)
        tabBar.addView(pastTab)
        headerLayout.addView(tabBar)

        mainContent.addView(headerLayout)

        // --- TICKETS LIST ---
        val scrollView = ScrollView(this).apply {
            isFillViewport = true
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                addRule(RelativeLayout.BELOW, headerLayout.id)
            }
        }

        ticketsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((16 * density).toInt(), (24 * density).toInt(), (16 * density).toInt(), (24 * density).toInt())
        }
        scrollView.addView(ticketsContainer)
        mainContent.addView(scrollView)

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
        mainContent.addView(progressBar)

        rootLayout.addView(mainContent)
        setContentView(rootLayout)

        // Inisialisasi awal
        updateTabUI()
        loadAllTickets()
    }

    private fun updateTabUI() {
        val density = resources.displayMetrics.density
        val activeBg = GradientDrawable().apply {
            setColor(Color.WHITE)
            cornerRadius = 20f * density
        }

        if (isShowingUpcoming) {
            upcomingTab.background = activeBg
            upcomingTab.setTextColor(wooshRed)
            pastTab.background = null
            pastTab.setTextColor(Color.WHITE)
        } else {
            pastTab.background = activeBg
            pastTab.setTextColor(wooshRed)
            upcomingTab.background = null
            upcomingTab.setTextColor(Color.WHITE)
        }
    }

    private fun loadAllTickets() {
        progressBar.visibility = View.VISIBLE
        ticketsContainer.removeAllViews()

        lifecycleScope.launch {
            try {
                // Ambil semua tiket (tanpa filter status secara spesifik di query, atau "paid/completed")
                val response = ApiClient.instance.getTicketHistory(token)
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    allTickets = response.body()!!.data ?: emptyList()
                    allTickets = response.body()!!.data ?: emptyList()
                    renderTickets()
                } else {
                    showEmptyState("Failed to load tickets.")
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                e.printStackTrace()
                showEmptyState("Error connecting to server.")
            }
        }
    }

    private fun renderTickets() {
        ticketsContainer.removeAllViews()
        val now = Date()

        // Filter berdasarkan waktu (Upcoming vs Past)
        val filteredTickets = allTickets.filter { ticket ->
            val depTimeStr = ticket.schedule?.departure?.time
            if (depTimeStr != null) {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                    val depDate = sdf.parse(depTimeStr)
                    if (isShowingUpcoming) {
                        depDate != null && depDate.after(now)
                    } else {
                        depDate != null && depDate.before(now)
                    }
                } catch (e: Exception) {
                    false
                }
            } else {
                false
            }
        }

        if (filteredTickets.isEmpty()) {
            val msg = if (isShowingUpcoming) "No upcoming tickets." else "No past tickets."
            showEmptyState(msg)
        } else {
            // Urutkan jadwal: Upcoming (terdekat dulu), Past (terbaru dulu)
            val sorted = if (isShowingUpcoming) {
                filteredTickets.sortedBy { it.schedule?.departure?.time }
            } else {
                filteredTickets.sortedByDescending { it.schedule?.departure?.time }
            }
            
            sorted.forEach { ticket ->
                addTicketCard(ticket)
            }
        }
    }

    private fun showEmptyState(message: String) {
        val density = resources.displayMetrics.density
        val tv = TextView(this).apply {
            text = message
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(textColorSecondary)
            setPadding(0, (40 * density).toInt(), 0, 0)
        }
        ticketsContainer.addView(tv)
    }

    private fun addTicketCard(ticket: TicketHistoryItem) {
        val density = resources.displayMetrics.density

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 16f * density
                setStroke((1 * density).toInt(), Color.parseColor("#FDE8E8")) // Very subtle red border
            }
            elevation = 2f * density
            setPadding((16 * density).toInt(), (16 * density).toInt(), (16 * density).toInt(), (16 * density).toInt())
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = (16 * density).toInt()
            }
        }

        // --- ROW 1: Icon, Train Code, Booking Code ---
        val topRow = RelativeLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        // Ikon Kereta
        val iconLayout = LinearLayout(this).apply {
            id = View.generateViewId()
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
        }
        val trainIcon = ImageView(this).apply {
            setImageResource(R.drawable.ic_train) // Make sure this icon exists
            setColorFilter(wooshRed)
            layoutParams = LinearLayout.LayoutParams((20 * density).toInt(), (20 * density).toInt())
        }
        val trainCodeText = TextView(this).apply {
            text = ticket.schedule?.trainName ?: "WT"
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(textColorPrimary)
            setPadding((8 * density).toInt(), 0, 0, 0)
        }
        iconLayout.addView(trainIcon)
        iconLayout.addView(trainCodeText)

        // Container untuk Status Badge & Booking Code
        val rightSidePills = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
        }

        // Status Badge
        val statusLabel = ticket.status?.uppercase() ?: "PENDING"
        val statusColor = when (statusLabel) {
            "COMPLETED", "PAID", "SUCCESS" -> Color.parseColor("#10B981") // Hijau
            "PENDING" -> Color.parseColor("#F59E0B") // Oranye
            "CANCELLED", "FAILED" -> Color.parseColor("#EF4444") // Merah
            else -> Color.parseColor("#6B7280") // Abu-abu
        }

        val statusBadge = TextView(this).apply {
            text = statusLabel
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                setColor(statusColor)
                cornerRadius = 8f * density
            }
            setPadding((6 * density).toInt(), (2 * density).toInt(), (6 * density).toInt(), (2 * density).toInt())
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                rightMargin = (6 * density).toInt()
            }
        }
        rightSidePills.addView(statusBadge)

        // Booking Code Pill
        val bookingCodeText = TextView(this).apply {
            text = ticket.bookingCode?.replace("WOOSH-", "") ?: "N/A"
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(textColorSecondary)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F3F4F6"))
                cornerRadius = 8f * density
            }
            setPadding((8 * density).toInt(), (4 * density).toInt(), (8 * density).toInt(), (4 * density).toInt())
        }
        rightSidePills.addView(bookingCodeText)

        topRow.addView(iconLayout)
        topRow.addView(rightSidePills)
        card.addView(topRow)

        // --- ROW 2: Departure, Timeline, Arrival ---
        val timelineRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = (24 * density).toInt()
                bottomMargin = (20 * density).toInt()
            }
        }

        // Dep
        val depLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val depTime = TextView(this).apply {
            val fullTime = ticket.schedule?.departure?.time ?: "00:00:00"
            text = fullTime.substringAfter(" ").substringBeforeLast(":") // "08:45"
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(textColorPrimary)
        }
        val depStation = TextView(this).apply {
            text = ticket.schedule?.departure?.stationName ?: "N/A"
            textSize = 12f
            setTextColor(textColorSecondary)
        }
        depLayout.addView(depTime)
        depLayout.addView(depStation)

        // Timeline Center
        val centerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.5f)
        }
        val lineView = View(this).apply {
            background = GradientDrawable().apply { setColor(borderColor) }
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (2 * density).toInt()).apply {
                setMargins(0, (10 * density).toInt(), 0, (4 * density).toInt())
            }
        }
        val durationText = TextView(this).apply {
            text = "⏱ 45m"
            textSize = 10f
            setTextColor(textColorSecondary)
        }
        centerLayout.addView(lineView)
        centerLayout.addView(durationText)

        // Arr
        val arrLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val arrTime = TextView(this).apply {
            val fullTime = ticket.schedule?.arrival?.time ?: "00:00:00"
            text = fullTime.substringAfter(" ").substringBeforeLast(":") // "09:30"
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(textColorPrimary)
        }
        val arrStation = TextView(this).apply {
            text = ticket.schedule?.arrival?.stationName ?: "N/A"
            textSize = 12f
            setTextColor(textColorSecondary)
        }
        arrLayout.addView(arrTime)
        arrLayout.addView(arrStation)

        timelineRow.addView(depLayout)
        timelineRow.addView(centerLayout)
        timelineRow.addView(arrLayout)
        card.addView(timelineRow)

        // --- DASHED DIVIDER ---
        val dashedLine = View(this).apply {
            val shapeDrawable = ShapeDrawable(RectShape())
            shapeDrawable.paint.color = borderColor
            shapeDrawable.paint.style = Paint.Style.STROKE
            shapeDrawable.paint.strokeWidth = 2f * density
            shapeDrawable.paint.pathEffect = DashPathEffect(floatArrayOf(10f * density, 10f * density), 0f)
            background = shapeDrawable
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (2 * density).toInt()).apply {
                bottomMargin = (16 * density).toInt()
            }
        }
        card.addView(dashedLine)

        // --- ROW 3: Seat Info & Date ---
        val bottomRow = RelativeLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        val seatLayout = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
            layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                addRule(RelativeLayout.ALIGN_PARENT_LEFT)
            }
        }
        val seatIcon = ImageView(this).apply {
            setImageResource(R.drawable.ic_seat) // Make sure this exists, or use ic_train as fallback
            setColorFilter(textColorSecondary)
            layoutParams = LinearLayout.LayoutParams((16 * density).toInt(), (16 * density).toInt())
        }
        val seatText = TextView(this).apply {
            val seatNumber = ticket.passengers?.firstOrNull()?.seat ?: "-"
            val trainCode = ticket.schedule?.trainName ?: "WT"
            text = "$trainCode, $seatNumber"
            textSize = 12f
            setTextColor(textColorSecondary)
            setPadding((6 * density).toInt(), 0, 0, 0)
        }
        seatLayout.addView(seatIcon)
        seatLayout.addView(seatText)

        val dateText = TextView(this).apply {
            val depTimeStr = ticket.schedule?.departure?.time
            var formattedDate = "N/A"
            if (depTimeStr != null) {
                try {
                    val sdfIn = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                    val sdfOut = SimpleDateFormat("EEE, dd MMM yyyy", Locale.US)
                    val date = sdfIn.parse(depTimeStr)
                    if (date != null) {
                        formattedDate = sdfOut.format(date)
                    }
                } catch (e: Exception) {}
            }
            text = formattedDate
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(textColorPrimary)
            layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
        }

        bottomRow.addView(seatLayout)
        bottomRow.addView(dateText)
        card.addView(bottomRow)

        // --- ON CLICK TO TICKET DETAIL ---
        card.setOnClickListener {
            val intent = Intent(this@MyTicketsActivity, TicketActivity::class.java).apply {
                putExtra("booking_id", ticket.bookingId)
                putExtra("order_id", ticket.bookingCode)
                putExtra("status", ticket.status)
                putExtra("total_amount", ticket.payment?.amount ?: 0)
                putExtra("departure_station", ticket.schedule?.departure?.stationName)
                putExtra("arrival_station", ticket.schedule?.arrival?.stationName)
                
                val rawTime = ticket.schedule?.departure?.time ?: "00:00"
                var displayDate = rawTime
                try {
                    val sdfIn = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                    val sdfOut = SimpleDateFormat("EEE, dd MMM yyyy", Locale.US)
                    val d = sdfIn.parse(rawTime)
                    if (d != null) displayDate = sdfOut.format(d)
                } catch (e: Exception) {}
                
                putExtra("date_display", displayDate)
                putExtra("departure_time", rawTime)
                putExtra("arrival_time", ticket.schedule?.arrival?.time)
                putExtra("train_code", ticket.schedule?.trainName)
                putExtra("train_class", "Premium Economy")
                putExtra("seat_number", ticket.passengers?.firstOrNull()?.seat)
                putExtra("passenger_name", ticket.passengers?.firstOrNull()?.name)
            }
            startActivity(intent)
        }

        ticketsContainer.addView(card)
    }
}
