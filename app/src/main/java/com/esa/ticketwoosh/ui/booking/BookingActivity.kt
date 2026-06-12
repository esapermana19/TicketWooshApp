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
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.cardview.widget.CardView // Add this import
import com.esa.ticketwoosh.R
import com.esa.ticketwoosh.data.api.ApiClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BookingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var autoScrollRunnable: Runnable

    // State untuk Stasiun (Data dari Database Laravel)
    private var stationList = ArrayList<String>() // Menampung nama-nama stasiun dari DB
    private var stationMap = HashMap<String, Int>() // Mapping nama ke ID stasiun
    private var selectedOrigin: String = "Halim (Jakarta)"
    private var selectedDestination: String = "Tegalluar (Bandung)"
    private var selectedDate: Calendar = Calendar.getInstance()
    private var passengerCount: Int = 1

    // View untuk Stasiun
    private lateinit var originStationTextView: TextView
    private lateinit var destinationStationTextView: TextView
    private lateinit var departureDateTextView: TextView
    private lateinit var passengerCountTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val density = resources.displayMetrics.density
        val wooshRed = Color.parseColor("#ED1C24") // True Whoosh red
        val bgColor = Color.parseColor("#F4F6FA")
        val textColorPrimary = Color.parseColor("#111827")
        val textColorSecondary = Color.parseColor("#6B7280")
        val cardBackgroundColor = Color.WHITE
        val borderColor = Color.parseColor("#E5E7EB")

        // Ambil data stasiun dari database Laravel secara real-time
        fetchStationData()

        val rootLayout = RelativeLayout(this).apply {
            setBackgroundColor(bgColor)
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // --- Bottom Navigation ---
        val bottomNavId = com.esa.ticketwoosh.utils.BottomNavigationUtil.setupBottomNav(this, rootLayout, 0)

        // --- ScrollView ---
        val scrollView = ScrollView(this).apply {
            isFillViewport = true
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                addRule(RelativeLayout.ABOVE, bottomNavId)
            }
        }
        
        val scrollContent = RelativeLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        
        // --- Top Header ---
        val topHeader = LinearLayout(this).apply {
            id = View.generateViewId()
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(wooshRed)
                cornerRadii = floatArrayOf(0f, 0f, 0f, 0f, 40f * density, 40f * density, 40f * density, 40f * density)
            }
            setPadding((24 * density).toInt(), (40 * density).toInt(), (24 * density).toInt(), (80 * density).toInt())
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val whooshTitle = TextView(this).apply {
            text = "Whoosh"
            textSize = 32f
            setTypeface(null, Typeface.BOLD_ITALIC)
            setTextColor(Color.WHITE)
        }

        val subtitle = TextView(this).apply {
            text = "Jakarta - Bandung High Speed Train"
            textSize = 14f
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = (4 * density).toInt() }
        }
        topHeader.addView(whooshTitle)
        topHeader.addView(subtitle)
        scrollContent.addView(topHeader)

        // --- Banner Carousel ---
        val bannerImages = listOf(
            R.drawable.banner1,
            R.drawable.banner2,
            R.drawable.banner6
        )

        val bannerContainer = RelativeLayout(this).apply {
            id = View.generateViewId()
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (180 * density).toInt()
            ).apply {
                addRule(RelativeLayout.BELOW, topHeader.id)
                topMargin = (-60 * density).toInt() // Overlap sedikit dengan header
                leftMargin = (24 * density).toInt()
                rightMargin = (24 * density).toInt()
            }
        }

        viewPager = ViewPager2(this).apply {
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            adapter = BannerAdapter(bannerImages)
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
        }

        // Rounded corners untuk ViewPager container
        val cardBanner = CardView(this).apply {
            radius = 16f * density
            cardElevation = 4f * density
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            addView(viewPager)
        }
        
        bannerContainer.addView(cardBanner)
        scrollContent.addView(bannerContainer)

        autoScrollRunnable = object : Runnable {
            override fun run() {
                val itemCount = viewPager.adapter?.itemCount ?: 0
                if (itemCount > 0) {
                    val nextItem = (viewPager.currentItem + 1) % itemCount
                    viewPager.setCurrentItem(nextItem, true)
                }
                handler.postDelayed(this, 3000) // Geser tiap 3 detik
            }
        }
        handler.postDelayed(autoScrollRunnable, 3000)
        
        val mainContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((24 * density).toInt(), 0, (24 * density).toInt(), (24 * density).toInt())
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.BELOW, bannerContainer.id)
                topMargin = (24 * density).toInt() 
            }
        }
        
        // --- Route Card Container ---
        val routeCardContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(cardBackgroundColor)
                cornerRadius = 16f * density
            }
            elevation = 6f * density
            setPadding((20 * density).toInt(), (20 * density).toInt(), (20 * density).toInt(), (20 * density).toInt())
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = (24 * density).toInt() }
        }

        val bookTicketTitle = TextView(this).apply {
            text = "Book Ticket"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#003366"))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = (16 * density).toInt() }
        }
        routeCardContainer.addView(bookTicketTitle)

        fun getBoxBackground(): GradientDrawable {
            return GradientDrawable().apply {
                setColor(Color.WHITE)
                setStroke((1 * density).toInt(), borderColor)
                cornerRadius = 12f * density
            }
        }

        // 1. Origin Block
        val originBlock = RelativeLayout(this).apply {
            background = getBoxBackground()
            setPadding((16 * density).toInt(), (12 * density).toInt(), (16 * density).toInt(), (12 * density).toInt())
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = (12 * density).toInt() }
            setOnClickListener { showStationDialog(true) }
        }
        
        val originIcon = ImageView(this).apply {
            id = View.generateViewId()
            setImageResource(R.drawable.ic_location)
            setColorFilter(wooshRed)
            layoutParams = RelativeLayout.LayoutParams((24 * density).toInt(), (24 * density).toInt()).apply { 
                addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
        }
        
        val originTextLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.RIGHT_OF, originIcon.id)
                leftMargin = (12 * density).toInt()
                rightMargin = (50 * density).toInt() // Ruang untuk tombol tukar
            }
        }
        val originLabel = TextView(this).apply {
            text = "From"
            textSize = 12f
            setTextColor(textColorSecondary)
        }
        originStationTextView = TextView(this).apply {
            text = selectedOrigin
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(textColorPrimary)
        }
        originTextLayout.addView(originLabel)
        originTextLayout.addView(originStationTextView)
        
        val swapButton = ImageView(this).apply {
            setImageResource(R.drawable.ic_swap)
            setColorFilter(textColorSecondary)
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                setStroke((1 * density).toInt(), borderColor)
                shape = GradientDrawable.OVAL
            }
            setPadding((6 * density).toInt(), (6 * density).toInt(), (6 * density).toInt(), (6 * density).toInt())
            layoutParams = RelativeLayout.LayoutParams((32 * density).toInt(), (32 * density).toInt()).apply {
                addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
            setOnClickListener {
                if (selectedOrigin != "Pilih Stasiun Asal" && selectedDestination != "Pilih Stasiun Tujuan" && selectedOrigin != selectedDestination) {
                    val temp = selectedOrigin
                    selectedOrigin = selectedDestination
                    selectedDestination = temp
                    originStationTextView.text = selectedOrigin
                    destinationStationTextView.text = selectedDestination
                } else {
                    Toast.makeText(context, "Pilih stasiun asal dan tujuan yang berbeda terlebih dahulu!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        originBlock.addView(originIcon)
        originBlock.addView(originTextLayout)
        originBlock.addView(swapButton)
        routeCardContainer.addView(originBlock)

        // 2. Destination Block
        val destinationBlock = RelativeLayout(this).apply {
            background = getBoxBackground()
            setPadding((16 * density).toInt(), (12 * density).toInt(), (16 * density).toInt(), (12 * density).toInt())
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = (16 * density).toInt() }
            setOnClickListener { showStationDialog(false) }
        }
        val destinationIcon = ImageView(this).apply {
            id = View.generateViewId()
            setImageResource(R.drawable.ic_location)
            setColorFilter(Color.parseColor("#4B5563")) // Grayish-black
            layoutParams = RelativeLayout.LayoutParams((24 * density).toInt(), (24 * density).toInt()).apply { 
                addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
        }
        val destinationTextLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.RIGHT_OF, destinationIcon.id)
                leftMargin = (12 * density).toInt()
            }
        }
        val destinationLabel = TextView(this).apply {
            text = "To"
            textSize = 12f
            setTextColor(textColorSecondary)
        }
        destinationStationTextView = TextView(this).apply {
            text = selectedDestination
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(textColorPrimary)
        }
        destinationTextLayout.addView(destinationLabel)
        destinationTextLayout.addView(destinationStationTextView)
        destinationBlock.addView(destinationIcon)
        destinationBlock.addView(destinationTextLayout)
        routeCardContainer.addView(destinationBlock)

        // 3. Date and Pax Section
        val datePaxSection = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = (20 * density).toInt() }
        }

        // --- DEPARTURE DATE ---
        val departureDateCard = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = getBoxBackground()
            setPadding((16 * density).toInt(), (12 * density).toInt(), (16 * density).toInt(), (12 * density).toInt())
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.2f).apply { 
                rightMargin = (12 * density).toInt() 
            }
            setOnClickListener {
                val datePickerDialog = android.app.DatePickerDialog(
                    this@BookingActivity,
                    { _, year, month, dayOfMonth ->
                        selectedDate.set(Calendar.YEAR, year)
                        selectedDate.set(Calendar.MONTH, month)
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        updateDateText()
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH)
                )
                // Hanya boleh pilih tanggal hari ini atau setelahnya
                datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
                datePickerDialog.show()
            }
        }
        val dateIcon = ImageView(this).apply {
            setImageResource(R.drawable.ic_calendar)
            setColorFilter(wooshRed)
            layoutParams = LinearLayout.LayoutParams((24 * density).toInt(), (24 * density).toInt()).apply {
                rightMargin = (8 * density).toInt()
            }
        }
        val dateTextLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val dateLabel = TextView(this).apply {
            text = "Departure"
            textSize = 12f
            setTextColor(textColorSecondary)
        }
        departureDateTextView = TextView(this).apply {
            text = "Wed, 18 Oct 2023"
            textSize = 14f
            setTextColor(textColorPrimary)
            typeface = Typeface.DEFAULT_BOLD
        }
        dateTextLayout.addView(dateLabel)
        dateTextLayout.addView(departureDateTextView)
        departureDateCard.addView(dateIcon)
        departureDateCard.addView(dateTextLayout)
        datePaxSection.addView(departureDateCard)

        // --- PAX COUNT ---
        val paxCard = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = getBoxBackground()
            setPadding((16 * density).toInt(), (12 * density).toInt(), (16 * density).toInt(), (12 * density).toInt())
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.8f)
            setOnClickListener { showPaxDialog() }
        }
        val paxIcon = ImageView(this).apply {
            setImageResource(R.drawable.ic_person)
            setColorFilter(wooshRed)
            layoutParams = LinearLayout.LayoutParams((24 * density).toInt(), (24 * density).toInt()).apply {
                rightMargin = (8 * density).toInt()
            }
        }
        val paxTextLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val paxLabel = TextView(this).apply {
            text = "Pax"
            textSize = 12f
            setTextColor(textColorSecondary)
        }
        passengerCountTextView = TextView(this).apply {
            text = passengerCount.toString()
            textSize = 14f
            setTextColor(textColorPrimary)
            typeface = Typeface.DEFAULT_BOLD
        }
        paxTextLayout.addView(paxLabel)
        paxTextLayout.addView(passengerCountTextView)
        paxCard.addView(paxIcon)
        paxCard.addView(paxTextLayout)
        datePaxSection.addView(paxCard)
        
        routeCardContainer.addView(datePaxSection)
        
        // 4. Search Trains Button
        val searchButton = Button(this).apply {
            text = "Search Trains"
            isAllCaps = false
            setTextColor(Color.WHITE)
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            background = GradientDrawable().apply {
                setColor(wooshRed)
                cornerRadius = 12f * density
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (56 * density).toInt()
            )
            setOnClickListener {
                if (selectedOrigin == "Pilih Stasiun Asal" || selectedDestination == "Pilih Stasiun Tujuan") {
                    Toast.makeText(this@BookingActivity, "Silakan pilih rute stasiun terlebih dahulu!", Toast.LENGTH_SHORT).show()
                } else if (selectedOrigin == selectedDestination) {
                    Toast.makeText(this@BookingActivity, "Stasiun asal dan tujuan tidak boleh sama!", Toast.LENGTH_SHORT).show()
                } else {
                    val originId = stationMap[selectedOrigin] ?: 1
                    val destinationId = stationMap[selectedDestination] ?: 4
                    val dateFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selectedDate.time)
                    
                    val intent = Intent(this@BookingActivity, ScheduleActivity::class.java).apply {
                        putExtra("ORIGIN_ID", originId)
                        putExtra("DESTINATION_ID", destinationId)
                        putExtra("ORIGIN_NAME", selectedOrigin)
                        putExtra("DESTINATION_NAME", selectedDestination)
                        putExtra("DATE_STR", dateFormatted)
                        putExtra("DATE_DISPLAY", SimpleDateFormat("EEE, dd MMM yyyy", Locale.US).format(selectedDate.time))
                        putExtra("PAX_COUNT", passengerCount)
                    }
                    startActivity(intent)
                }
            }
        }
        routeCardContainer.addView(searchButton)
        mainContent.addView(routeCardContainer)
        scrollContent.addView(mainContent)
        scrollView.addView(scrollContent)
        rootLayout.addView(scrollView)
        
        setContentView(rootLayout)

        updateDateText()
    }

    // ==========================================
    // LOGIKA MENAMPILKAN DIALOG PILIH STASIUN
    // ==========================================
    private fun showStationDialog(isOrigin: Boolean) {
        if (stationList.isEmpty()) {
            Toast.makeText(this, "Data stasiun belum siap / gagal dimuat dari server.", Toast.LENGTH_SHORT).show()
            return
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle(if (isOrigin) "Pilih Stasiun Asal" else "Pilih Stasiun Tujuan")

        // Memfilter daftar stasiun agar stasiun asal yang sudah dipilih tidak muncul di dialog tujuan, dan sebaliknya
        val filteredList = if (isOrigin) {
            stationList.filter { it != selectedDestination }
        } else {
            stationList.filter { it != selectedOrigin }
        }

        val stationsArray = filteredList.toTypedArray()
        builder.setItems(stationsArray) { _, which ->
            val chosenStation = stationsArray[which]
            if (isOrigin) {
                selectedOrigin = chosenStation
                originStationTextView.text = selectedOrigin
            } else {
                selectedDestination = chosenStation
                destinationStationTextView.text = chosenStation
            }
        }
        builder.show()
    }
    
    // ==========================================
    // LOGIKA MENAMPILKAN DIALOG PILIH PENUMPANG
    // ==========================================
    private fun showPaxDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pilih Jumlah Penumpang")
        val paxOptions = arrayOf("1", "2", "3", "4", "5")
        builder.setItems(paxOptions) { _, which ->
            passengerCount = paxOptions[which].toInt()
            passengerCountTextView.text = passengerCount.toString()
        }
        builder.show()
    }

    // ==========================================
    // LOGIKA HIT API LARAVEL UNTUK AMBIL DATA STASIUN
    // ==========================================
    private fun fetchStationData() {
        // Mode aman: Siapkan data backup lokal sekiranya server mati/offline saat dicoba
        stationList.clear()
        stationList.add("Halim (Jakarta)")
        stationList.add("Karawang")
        stationList.add("Padalarang")
        stationList.add("Tegalluar (Bandung)")

        stationMap.clear()
        stationMap["Halim (Jakarta)"] = 1
        stationMap["Karawang"] = 2
        stationMap["Padalarang"] = 3
        stationMap["Tegalluar (Bandung)"] = 4

        lifecycleScope.launch {
            try {
                // PANGGILAN API RETROFIT
                // Catatan: Pastikan di ApiService.kt Anda sudah menambahkan fungsi getStations()
                val response = ApiClient.instance.getStations()

                if (response.isSuccessful && response.body() != null) {
                    val incomingStations = response.body()!!
                    stationList.clear() // Bersihkan data cadangan lokal
                    stationMap.clear()

                    // Masukkan seluruh data stasiun murni dari database Laravel Anda
                    for (station in incomingStations) {
                        stationList.add(station.name) // Menyesuaikan field 'name' stasiun Anda
                        stationMap[station.name] = station.id
                    }
                }
            } catch (e: Exception) {
                // Tetap gunakan data lokal default jika API tidak terjangkau (offline mode)
                e.printStackTrace()
            }
        }
    }

    private fun updateDateText() {
        val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale("en", "US")) // To match "Wed, 18 Oct 2023"
        departureDateTextView.text = dateFormat.format(selectedDate.time)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(autoScrollRunnable)
    }

    inner class BannerAdapter(private val images: List<Int>) : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {
        inner class BannerViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
            val imageView = ImageView(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            return BannerViewHolder(imageView)
        }

        override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
            holder.imageView.setImageResource(images[position])
        }

        override fun getItemCount(): Int = images.size
    }
}