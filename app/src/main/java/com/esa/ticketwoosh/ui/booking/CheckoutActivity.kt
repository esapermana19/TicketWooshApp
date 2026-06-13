package com.esa.ticketwoosh.ui.booking

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

/**
 * CheckoutActivity — Passenger Details screen.
 * Displays the selected schedule summary at the top, then a form for each passenger
 * (ID Type, ID Number, Full Name), an info notice, and a "Pilih Kursi" button.
 */
class CheckoutActivity : AppCompatActivity() {

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

    // Colors
    private val wooshRed = Color.parseColor("#E01A22")
    private val wooshRedDark = Color.parseColor("#C41219")
    private val colorWhite = Color.WHITE
    private val colorBg = Color.parseColor("#F5F5F5")
    private val colorCardBg = Color.WHITE
    private val colorBorder = Color.parseColor("#E0E0E0")
    private val colorTextPrimary = Color.parseColor("#1A1A1A")
    private val colorTextSecondary = Color.parseColor("#6B7280")
    private val colorTextLink = Color.parseColor("#E01A22")
    private val colorInfoBg = Color.parseColor("#EFF6FF")
    private val colorInfoBorder = Color.parseColor("#BFDBFE")
    private val colorInfoText = Color.parseColor("#1D4ED8")

    // Passenger field references — list of Triple(idTypeSpinner, idNumberEdit, nameEdit)
    private val passengerFields = mutableListOf<Triple<Spinner, EditText, EditText>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Read extras
        scheduleId = intent.getIntExtra("SCHEDULE_ID", -1)
        trainId = intent.getIntExtra("TRAIN_ID", -1)
        passengerCount = intent.getIntExtra("PASSENGER_COUNT", 1)
        trainCode = intent.getStringExtra("TRAIN_CODE") ?: "G7705"
        trainClass = intent.getStringExtra("TRAIN_CLASS") ?: "Premium Economy"
        departureTime = intent.getStringExtra("DEPARTURE_TIME") ?: "08:45"
        arrivalTime = intent.getStringExtra("ARRIVAL_TIME") ?: "09:30"
        departureStation = intent.getStringExtra("DEPARTURE_STATION") ?: "HLM"
        arrivalStation = intent.getStringExtra("ARRIVAL_STATION") ?: "TGL"
        price = intent.getStringExtra("PRICE") ?: ""
        dateDisplay = intent.getStringExtra("DATE_DISPLAY") ?: ""

        val d = resources.displayMetrics.density

        // ── ROOT: CoordinatorLayout-like structure using RelativeLayout ──────────────
        val root = RelativeLayout(this).apply {
            setBackgroundColor(colorBg)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // ── 1. RED HEADER TOOLBAR ─────────────────────────────────────────────────────
        val toolbar = buildToolbar(d)
        toolbar.id = View.generateViewId()
        root.addView(toolbar)

        // ── 2. BOTTOM BUTTON (added before scroll so scroll can align above it) ──────
        val bottomBtn = buildBottomButton(d)
        bottomBtn.id = View.generateViewId()
        val bottomBtnParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { addRule(RelativeLayout.ALIGN_PARENT_BOTTOM) }
        bottomBtn.layoutParams = bottomBtnParams
        root.addView(bottomBtn)

        // ── 3. SCROLLABLE CONTENT ─────────────────────────────────────────────────────
        val scrollView = ScrollView(this).apply {
            id = View.generateViewId()
            isFillViewport = true
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                addRule(RelativeLayout.BELOW, toolbar.id)
                addRule(RelativeLayout.ABOVE, bottomBtn.id)
            }
        }

        val contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((16 * d).toInt(), (16 * d).toInt(), (16 * d).toInt(), (24 * d).toInt())
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Schedule summary card
        contentLayout.addView(buildScheduleCard(d))
        contentLayout.addView(spacer(d, 16))

        // Passenger cards
        for (i in 1..passengerCount) {
            contentLayout.addView(buildPassengerCard(d, i))
            if (i < passengerCount) contentLayout.addView(spacer(d, 12))
        }

        contentLayout.addView(spacer(d, 16))

        // Info notice
        contentLayout.addView(buildInfoNotice(d))

        scrollView.addView(contentLayout)
        root.addView(scrollView)

        setContentView(root)

        // Bottom button action
        bottomBtn.setOnClickListener { onSelectSeatClicked() }
    }

    // ──────────────────────────────────────────────────────────────────────────────────
    // Builder helpers
    // ──────────────────────────────────────────────────────────────────────────────────

    /** Red header toolbar with back arrow and title "Passenger Details" */
    private fun buildToolbar(d: Float): LinearLayout {
        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(wooshRed)
            setPadding(
                (4 * d).toInt(),
                (statusBarHeight() + (8 * d).toInt()),
                (16 * d).toInt(),
                (12 * d).toInt()
            )
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { addRule(RelativeLayout.ALIGN_PARENT_TOP) }
            elevation = 4 * d
        }

        // Back button (chevron)
        val backBtn = TextView(this).apply {
            text = "‹"
            textSize = 30f
            setTextColor(colorWhite)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding((12 * d).toInt(), 0, (12 * d).toInt(), 0)
            setOnClickListener { finish() }
        }
        toolbar.addView(backBtn)

        // Title
        val title = TextView(this).apply {
            text = "Passenger Details"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(colorWhite)
        }
        toolbar.addView(title)

        return toolbar
    }

    /** Schedule summary card: "G7705 • Premium Economy  [Details link]"
     *  "08:45 → 09:30   [date]" */
    private fun buildScheduleCard(d: Float): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = buildCardBackground(d)
            elevation = 2 * d
            setPadding((16 * d).toInt(), (14 * d).toInt(), (16 * d).toInt(), (14 * d).toInt())
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Row 1: train code • class + "Details" link
        val row1 = RelativeLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val trainInfo = TextView(this).apply {
            val className = toTitleCase(trainClass)
            text = "$trainCode • $className"
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(colorTextPrimary)
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { addRule(RelativeLayout.ALIGN_PARENT_START) }
        }
        row1.addView(trainInfo)

        val detailsLink = TextView(this).apply {
            text = "Details"
            textSize = 13f
            setTextColor(colorTextLink)
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { addRule(RelativeLayout.ALIGN_PARENT_END) }
        }
        row1.addView(detailsLink)
        card.addView(row1)
        card.addView(spacer(d, 6))

        // Row 2: times + date
        val row2 = RelativeLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val timesText = TextView(this).apply {
            text = "$departureTime  →  $arrivalTime"
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(colorTextPrimary)
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { addRule(RelativeLayout.ALIGN_PARENT_START) }
        }
        row2.addView(timesText)

        // Format date: "Wed, 18 Oct" style — use dateDisplay or derive from it
        val shortDate = formatShortDate(dateDisplay)
        val dateText = TextView(this).apply {
            text = shortDate
            textSize = 13f
            setTextColor(colorTextSecondary)
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { addRule(RelativeLayout.ALIGN_PARENT_END) }
        }
        row2.addView(dateText)
        card.addView(row2)

        return card
    }

    /** Builds a passenger detail card with section header and input fields */
    private fun buildPassengerCard(d: Float, index: Int): LinearLayout {
        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Section header: "Passenger X (Adult)"
        val sectionHeader = TextView(this).apply {
            text = "Passenger $index (Adult)"
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(colorTextPrimary)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = (10 * d).toInt() }
        }
        wrapper.addView(sectionHeader)

        // Card
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = buildCardBackground(d)
            elevation = 2 * d
            setPadding((16 * d).toInt(), (16 * d).toInt(), (16 * d).toInt(), (16 * d).toInt())
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // --- ID Type (Spinner displayed as labeled field) ---
        val idTypeOptions = arrayOf("KTP (Indonesian ID)", "Passport", "SIM (Driver's License)")
        val idTypeSpinner = buildLabeledSpinner(d, "ID Type", idTypeOptions)

        // --- ID Number ---
        val idNumberEdit = buildLabeledEditText(
            d, "ID Number", "e.g. 3171234567890001",
            InputType.TYPE_CLASS_NUMBER, "idNumber_$index"
        )

        // --- Full Name ---
        val fullNameEdit = buildLabeledEditText(
            d, "Full Name", "Enter full name as on ID",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS, "fullName_$index"
        )

        card.addView(idTypeSpinner.first)      // label+spinner wrapper
        card.addView(spacer(d, 12))
        card.addView(idNumberEdit.first)       // label+edit wrapper
        card.addView(spacer(d, 12))
        card.addView(fullNameEdit.first)       // label+edit wrapper

        wrapper.addView(card)

        // Store for collection later
        passengerFields.add(Triple(idTypeSpinner.second, idNumberEdit.second, fullNameEdit.second))

        return wrapper
    }

    /** Info notice with shield icon and text */
    private fun buildInfoNotice(d: Float): LinearLayout {
        val notice = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.TOP
            background = GradientDrawable().apply {
                setColor(colorInfoBg)
                setStroke((1 * d).toInt(), colorInfoBorder)
                cornerRadius = 12f * d
            }
            setPadding((14 * d).toInt(), (12 * d).toInt(), (14 * d).toInt(), (12 * d).toInt())
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Shield/info icon
        val icon = TextView(this).apply {
            text = "🛡️"
            textSize = 18f
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { rightMargin = (10 * d).toInt() }
        }
        notice.addView(icon)

        val noticeText = TextView(this).apply {
            text = "Please ensure all passenger details match the official ID. You will need to present it during boarding."
            textSize = 13f
            setTextColor(colorInfoText)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        notice.addView(noticeText)

        return notice
    }

    /** "Pilih Kursi" bottom sticky button */
    private fun buildBottomButton(d: Float): LinearLayout {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#111111"))
            setPadding((16 * d).toInt(), (12 * d).toInt(), (16 * d).toInt(), (20 * d).toInt())
        }

        // Add Total Price Row
        val totalRow = RelativeLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = (12 * d).toInt()
            }
        }
        val totalLabel = TextView(this).apply {
            text = "Total Payment"
            textSize = 14f
            setTextColor(Color.parseColor("#AAAAAA"))
            layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                addRule(RelativeLayout.ALIGN_PARENT_START)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
        }
        val totalPriceTxt = TextView(this).apply {
            val total = (price.toDoubleOrNull() ?: 0.0) * passengerCount
            text = "Rp " + java.text.NumberFormat.getNumberInstance(java.util.Locale("id", "ID")).format(total)
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                addRule(RelativeLayout.ALIGN_PARENT_END)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
        }
        totalRow.addView(totalLabel)
        totalRow.addView(totalPriceTxt)
        container.addView(totalRow)

        val btn = TextView(this).apply {
            text = "Select Seat"
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(colorWhite)
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                setColor(wooshRed)
                cornerRadius = 24f * d // Rounded corners
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (48 * d).toInt()
            )
            setOnClickListener { onSelectSeatClicked() }
        }
        container.addView(btn)
        return container
    }

    // ──────────────────────────────────────────────────────────────────────────────────
    // Field builders
    // ──────────────────────────────────────────────────────────────────────────────────

    /** Returns Pair(wrapper LinearLayout, Spinner) */
    private fun buildLabeledSpinner(d: Float, label: String, options: Array<String>): Pair<LinearLayout, Spinner> {
        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        val labelView = buildLabel(d, label)
        wrapper.addView(labelView)
        wrapper.addView(spacer(d, 4))

        val spinner = Spinner(this).apply {
            background = buildFieldBackground(d)
            setPadding((12 * d).toInt(), (12 * d).toInt(), (12 * d).toInt(), (12 * d).toInt())
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (52 * d).toInt()
            )
            adapter = object : ArrayAdapter<String>(this@CheckoutActivity, android.R.layout.simple_spinner_item, options) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent) as TextView
                    view.setTextColor(colorTextPrimary)
                    return view
                }
                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getDropDownView(position, convertView, parent) as TextView
                    view.setTextColor(colorTextPrimary)
                    return view
                }
            }.also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        }
        wrapper.addView(spinner)
        return Pair(wrapper, spinner)
    }

    /** Returns Pair(wrapper LinearLayout, EditText) */
    private fun buildLabeledEditText(
        d: Float, label: String, hint: String, inputType: Int, tag: String
    ): Pair<LinearLayout, EditText> {
        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        wrapper.addView(buildLabel(d, label))
        wrapper.addView(spacer(d, 4))

        val editText = EditText(this).apply {
            this.hint = hint
            this.inputType = inputType
            this.tag = tag
            textSize = 15f
            setTextColor(colorTextPrimary)
            setHintTextColor(Color.parseColor("#AAAAAA"))
            background = buildFieldBackground(d)
            setPadding((14 * d).toInt(), (12 * d).toInt(), (14 * d).toInt(), (12 * d).toInt())
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (52 * d).toInt()
            )
        }
        wrapper.addView(editText)
        return Pair(wrapper, editText)
    }

    private fun buildLabel(d: Float, text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 12f
            setTextColor(colorTextSecondary)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    // ──────────────────────────────────────────────────────────────────────────────────
    // Action
    // ──────────────────────────────────────────────────────────────────────────────────

    private fun onSelectSeatClicked() {
        val passengers = mutableListOf<Map<String, String>>()
        for ((i, triple) in passengerFields.withIndex()) {
            val (spinner, idEdit, nameEdit) = triple
            val idType = spinner.selectedItem?.toString() ?: ""
            val idNumber = idEdit.text?.toString()?.trim() ?: ""
            val fullName = nameEdit.text?.toString()?.trim() ?: ""

            if (idNumber.isEmpty()) {
                Toast.makeText(this, "Harap isi Nomor ID penumpang ${i + 1}", Toast.LENGTH_SHORT).show()
                return
            }
            if (fullName.isEmpty()) {
                Toast.makeText(this, "Harap isi Nama Lengkap penumpang ${i + 1}", Toast.LENGTH_SHORT).show()
                return
            }
            passengers.add(mapOf("idType" to idType, "idNumber" to idNumber, "name" to fullName))
        }

        val intent = Intent(this, SeatActivity::class.java).apply {
            putExtra("SCHEDULE_ID", scheduleId)
            putExtra("TRAIN_ID", trainId)
            putExtra("PASSENGER_COUNT", passengerCount)
            putExtra("TRAIN_CODE", trainCode)
            putExtra("TRAIN_CLASS", trainClass)
            putExtra("DEPARTURE_TIME", departureTime)
            putExtra("ARRIVAL_TIME", arrivalTime)
            putExtra("DEPARTURE_STATION", departureStation)
            putExtra("ARRIVAL_STATION", arrivalStation)
            putExtra("PRICE", price)
            putExtra("DATE_DISPLAY", dateDisplay)
            putStringArrayListExtra("PASSENGER_NAMES", ArrayList(passengers.map { it["name"] ?: "" }))
            putStringArrayListExtra("PASSENGER_IDS", ArrayList(passengers.map { it["idNumber"] ?: "" }))
            putStringArrayListExtra("PASSENGER_TYPES", ArrayList(passengers.map { it["idType"] ?: "" }))
        }
        startActivity(intent)
    }

    // ──────────────────────────────────────────────────────────────────────────────────
    // Drawable helpers
    // ──────────────────────────────────────────────────────────────────────────────────

    private fun buildCardBackground(d: Float): GradientDrawable {
        return GradientDrawable().apply {
            setColor(colorCardBg)
            cornerRadius = 12f * d
            setStroke((1 * d).toInt(), colorBorder)
        }
    }

    private fun buildFieldBackground(d: Float): GradientDrawable {
        return GradientDrawable().apply {
            setColor(colorWhite)
            cornerRadius = 8f * d
            setStroke((1 * d).toInt(), colorBorder)
        }
    }

    private fun spacer(d: Float, heightDp: Int): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (heightDp * d).toInt()
            )
        }
    }

    // ──────────────────────────────────────────────────────────────────────────────────
    // Utility
    // ──────────────────────────────────────────────────────────────────────────────────

    private fun statusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

    /** Convert "Wed, 09 Jun 2026" → "Wed, 09 Jun" or keep as-is */
    private fun formatShortDate(fullDate: String): String {
        if (fullDate.isBlank()) return ""
        // e.g. "Wed, 09 Jun 2026" → "Wed, 09 Jun"
        val parts = fullDate.split(",")
        if (parts.size == 2) {
            val dayParts = parts[1].trim().split(" ")
            if (dayParts.size >= 3) {
                return "${parts[0].trim()}, ${dayParts[0]} ${dayParts[1]}"
            }
        }
        return fullDate
    }

    private fun toTitleCase(input: String): String {
        return input.split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
    }
}
