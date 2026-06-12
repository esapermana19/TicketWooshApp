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
import com.esa.ticketwoosh.data.model.TicketHistoryItem
import com.esa.ticketwoosh.utils.BottomNavigationUtil
import kotlinx.coroutines.launch
import com.esa.ticketwoosh.ui.booking.TicketActivity

class MyTicketsActivity : AppCompatActivity() {
    private lateinit var ticketsContainer: LinearLayout
    private lateinit var filterSpinner: Spinner
    private var token: String = "" // Should be fetched from SessionManager, assuming Bearer token here

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Fetch token from SessionManager
        val sessionManager = com.esa.ticketwoosh.utils.SessionManager(this)
        val savedToken = sessionManager.fetchAuthToken() ?: ""
        token = "Bearer $savedToken"

        val density = resources.displayMetrics.density
        val bgColor = Color.parseColor("#F4F6FA")

        val rootLayout = RelativeLayout(this).apply {
            setBackgroundColor(bgColor)
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val bottomNavId = BottomNavigationUtil.setupBottomNav(this, rootLayout, 1)

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                addRule(RelativeLayout.ABOVE, bottomNavId)
            }
        }

        // Header
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding((20 * density).toInt(), (20 * density).toInt(), (20 * density).toInt(), (10 * density).toInt())
            gravity = Gravity.CENTER_VERTICAL
        }

        val title = TextView(this).apply {
            text = "My Tickets"
            textSize = 24f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#1C1C1E"))
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        val filters = arrayOf("All", "Pending", "Paid", "Failed", "Completed")
        filterSpinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@MyTicketsActivity, android.R.layout.simple_spinner_dropdown_item, filters)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val filter = filters[position].lowercase()
                    loadTickets(filter)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        headerLayout.addView(title)
        headerLayout.addView(filterSpinner)
        mainLayout.addView(headerLayout)

        // ScrollView for list
        val scrollView = ScrollView(this).apply {
            isFillViewport = true
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        ticketsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((20 * density).toInt(), 0, (20 * density).toInt(), (20 * density).toInt())
        }

        scrollView.addView(ticketsContainer)
        mainLayout.addView(scrollView)

        rootLayout.addView(mainLayout)
        setContentView(rootLayout)
    }

    private fun loadTickets(filter: String) {
        ticketsContainer.removeAllViews()
        val loadingText = TextView(this).apply {
            text = "Loading..."
            gravity = Gravity.CENTER
            setPadding(0, 50, 0, 0)
        }
        ticketsContainer.addView(loadingText)

        lifecycleScope.launch {
            try {
                val response = if (filter == "all") {
                    ApiClient.instance.getTicketHistory(token)
                } else {
                    ApiClient.instance.getFilteredTicketHistory(token, filter)
                }

                ticketsContainer.removeAllViews()

                if (response.isSuccessful && response.body() != null) {
                    val tickets = response.body()!!.data
                    if (tickets.isNullOrEmpty()) {
                        ticketsContainer.addView(TextView(this@MyTicketsActivity).apply {
                            text = "No tickets found."
                            gravity = Gravity.CENTER
                            setPadding(0, 50, 0, 0)
                        })
                    } else {
                        tickets.forEach { ticket ->
                            addTicketCard(ticket)
                        }
                    }
                } else {
                    ticketsContainer.addView(TextView(this@MyTicketsActivity).apply {
                        text = "Failed to load tickets."
                        gravity = Gravity.CENTER
                        setPadding(0, 50, 0, 0)
                    })
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ticketsContainer.removeAllViews()
                ticketsContainer.addView(TextView(this@MyTicketsActivity).apply {
                    text = "Error connecting to server."
                    gravity = Gravity.CENTER
                    setPadding(0, 50, 0, 0)
                })
            }
        }
    }

    private fun addTicketCard(ticket: TicketHistoryItem) {
        val density = resources.displayMetrics.density
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundDrawable(GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 16 * density
            })
            setPadding((16 * density).toInt(), (16 * density).toInt(), (16 * density).toInt(), (16 * density).toInt())
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, (15 * density).toInt())
            }
            elevation = 4f * density
        }

        val header = RelativeLayout(this)
        val orderId = TextView(this).apply {
            text = "Order: ${ticket.bookingCode ?: "N/A"}"
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#1C1C1E"))
        }
        val status = TextView(this).apply {
            text = ticket.status?.uppercase() ?: "UNKNOWN"
            textSize = 12f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setPadding((8 * density).toInt(), (4 * density).toInt(), (8 * density).toInt(), (4 * density).toInt())
            val statusColor = when(ticket.status?.lowercase()) {
                "completed", "paid" -> "#28A745"
                "pending" -> "#FFC107"
                "failed" -> "#DC3545"
                else -> "#6C757D"
            }
            setBackgroundDrawable(GradientDrawable().apply {
                setColor(Color.parseColor(statusColor))
                cornerRadius = 8 * density
            })
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            }
        }
        header.addView(orderId)
        header.addView(status)
        card.addView(header)

        val detailLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, (10 * density).toInt(), 0, 0)
        }
        
        detailLayout.addView(TextView(this).apply {
            val seatInfo = ticket.passengers?.firstOrNull()?.seat ?: "-"
            text = "Seat: $seatInfo"
            setTextColor(Color.parseColor("#6C757D"))
        })
        detailLayout.addView(TextView(this).apply {
            text = "Total: Rp ${ticket.payment?.amount ?: 0}"
            setTextColor(Color.parseColor("#6C757D"))
        })
        card.addView(detailLayout)
        
        // Klik seluruh kartu untuk membuka faktur
        card.setOnClickListener {
            val intent = Intent(this@MyTicketsActivity, TicketActivity::class.java).apply {
                putExtra("booking_id", ticket.bookingId)
                putExtra("order_id", ticket.bookingCode)
                putExtra("status", ticket.status)
                putExtra("total_amount", ticket.payment?.amount ?: 0)
                putExtra("departure_station", ticket.schedule?.departure?.stationName)
                putExtra("arrival_station", ticket.schedule?.arrival?.stationName)
                putExtra("date_display", ticket.schedule?.departure?.time)
                putExtra("departure_time", ticket.schedule?.departure?.time)
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
