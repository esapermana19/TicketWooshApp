package com.esa.ticketwoosh.ui.dashboard

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.esa.ticketwoosh.R
import com.esa.ticketwoosh.data.api.ApiClient
import com.esa.ticketwoosh.data.model.ProfileUpdateRequest
import com.esa.ticketwoosh.ui.auth.LoginActivity
import com.esa.ticketwoosh.utils.BottomNavigationUtil
import com.esa.ticketwoosh.utils.SessionManager
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvFullName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(this)
        token = "Bearer " + (sessionManager.fetchAuthToken() ?: "")

        val density = resources.displayMetrics.density
        val bgColor = Color.parseColor("#F4F6FA")
        val wooshRed = Color.parseColor("#E01A22")

        val rootLayout = RelativeLayout(this).apply {
            setBackgroundColor(bgColor)
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val bottomNavId = BottomNavigationUtil.setupBottomNav(this, rootLayout, 2)

        val scrollView = ScrollView(this).apply {
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                addRule(RelativeLayout.ABOVE, bottomNavId)
            }
            isFillViewport = true
        }

        val contentContainer = RelativeLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Red Banner
        val redBanner = RelativeLayout(this).apply {
            id = View.generateViewId()
            background = GradientDrawable().apply {
                setColor(wooshRed)
                // TopLeft, TopRight, BottomRight, BottomLeft
                cornerRadii = floatArrayOf(
                    0f, 0f,
                    0f, 0f,
                    30f * density, 30f * density,
                    30f * density, 30f * density
                )
            }
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (200 * density).toInt()
            )
        }

        val title = TextView(this).apply {
            text = "Account Profile"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                setMargins((20 * density).toInt(), (40 * density).toInt(), 0, 0)
            }
        }
        redBanner.addView(title)

        val btnEdit = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_edit)
            setColorFilter(Color.WHITE)
            setPadding((8 * density).toInt(), (8 * density).toInt(), (8 * density).toInt(), (8 * density).toInt())
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#C1171D")) // Darker red
                cornerRadius = 20 * density
            }
            layoutParams = RelativeLayout.LayoutParams(
                (40 * density).toInt(),
                (40 * density).toInt()
            ).apply {
                addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                setMargins(0, (30 * density).toInt(), (20 * density).toInt(), 0)
            }
            setOnClickListener { showEditProfileDialog() }
        }
        redBanner.addView(btnEdit)

        contentContainer.addView(redBanner)

        // White Card Container
        val cardLayout = LinearLayout(this).apply {
            id = View.generateViewId()
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 16 * density
            }
            elevation = 8f * density
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins((20 * density).toInt(), (100 * density).toInt(), (20 * density).toInt(), 0)
            }
            setPadding((15 * density).toInt(), (10 * density).toInt(), (15 * density).toInt(), (10 * density).toInt())
        }

        // --- ROW FULL NAME ---
        val rowName = createProfileRow(density, R.drawable.ic_profil, "Full Name", "Loading...")
        tvFullName = rowName.findViewById(2)
        cardLayout.addView(rowName)
        cardLayout.addView(createDivider(density))

        // --- ROW EMAIL ---
        val rowEmail = createProfileRow(density, android.R.drawable.ic_dialog_email, "Email Address", "Loading...")
        tvEmail = rowEmail.findViewById(2)
        cardLayout.addView(rowEmail)
        cardLayout.addView(createDivider(density))

        // --- ROW PHONE ---
        val rowPhone = createProfileRow(density, android.R.drawable.ic_menu_call, "Phone Number", "Loading...")
        tvPhone = rowPhone.findViewById(2)
        cardLayout.addView(rowPhone)
        cardLayout.addView(createDivider(density))

        // --- ROW PASSWORD ---
        val rowPassword = createProfileRow(density, android.R.drawable.ic_lock_idle_lock, "Password", "••••••••")
        // Add Change button to password row
        val btnChange = TextView(this).apply {
            text = "Change"
            textSize = 12f
            setTypeface(null, Typeface.BOLD)
            setTextColor(wooshRed)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#FFF0F0"))
                cornerRadius = 8 * density
            }
            setPadding((12 * density).toInt(), (6 * density).toInt(), (12 * density).toInt(), (6 * density).toInt())
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_VERTICAL
                setMargins((10 * density).toInt(), 0, 0, 0)
            }
            setOnClickListener { showChangePasswordDialog() }
        }
        rowPassword.addView(btnChange)
        cardLayout.addView(rowPassword)

        contentContainer.addView(cardLayout)

        // Log Out Button
        val btnLogout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                setStroke((1 * density).toInt(), Color.parseColor("#E0E0E0"))
                cornerRadius = 12 * density
            }
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (50 * density).toInt()
            ).apply {
                addRule(RelativeLayout.BELOW, cardLayout.id)
                setMargins((20 * density).toInt(), (20 * density).toInt(), (20 * density).toInt(), (30 * density).toInt())
            }
            setOnClickListener {
                AlertDialog.Builder(this@ProfileActivity)
                    .setTitle("Konfirmasi Logout")
                    .setMessage("Yakin mau logout?")
                    .setPositiveButton("Ya") { _, _ ->
                        sessionManager.clearSession()
                        val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }
        }

        val icLogout = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_revert)
            setColorFilter(wooshRed)
            layoutParams = LinearLayout.LayoutParams(
                (20 * density).toInt(),
                (20 * density).toInt()
            ).apply {
                setMargins(0, 0, (8 * density).toInt(), 0)
            }
        }
        val tvLogout = TextView(this).apply {
            text = "Log Out"
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
        }
        btnLogout.addView(icLogout)
        btnLogout.addView(tvLogout)

        contentContainer.addView(btnLogout)

        scrollView.addView(contentContainer)
        rootLayout.addView(scrollView)
        setContentView(rootLayout)

        loadProfile()
    }

    private fun createProfileRow(density: Float, iconResId: Int, labelStr: String, valueStr: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, (15 * density).toInt(), 0, (15 * density).toInt())

            // Icon Container
            val iconContainer = FrameLayout(this.context).apply {
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#F4F6FA"))
                    cornerRadius = 12 * density
                }
                layoutParams = LinearLayout.LayoutParams(
                    (45 * density).toInt(),
                    (45 * density).toInt()
                ).apply {
                    setMargins(0, 0, (15 * density).toInt(), 0)
                }
                
                val icon = ImageView(this.context).apply {
                    setImageResource(iconResId)
                    setColorFilter(Color.parseColor("#1C1C1E"))
                    layoutParams = FrameLayout.LayoutParams(
                        (20 * density).toInt(),
                        (20 * density).toInt()
                    ).apply {
                        gravity = Gravity.CENTER
                    }
                }
                addView(icon)
            }
            addView(iconContainer)

            // Text Container
            val textContainer = LinearLayout(this.context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    gravity = Gravity.CENTER_VERTICAL
                }

                val label = TextView(this.context).apply {
                    text = labelStr
                    textSize = 12f
                    setTextColor(Color.parseColor("#6C757D"))
                }
                addView(label)

                val value = TextView(this.context).apply {
                    id = 2 // Hardcoded ID for easy retrieval
                    text = valueStr
                    textSize = 14f
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(Color.parseColor("#1C1C1E"))
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, (2 * density).toInt(), 0, 0)
                    }
                }
                addView(value)
            }
            addView(textContainer)
        }
    }

    private fun createDivider(density: Float): View {
        return View(this).apply {
            setBackgroundColor(Color.parseColor("#F0F0F0"))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (1 * density).toInt()
            )
        }
    }

    private fun showEditProfileDialog() {
        val density = resources.displayMetrics.density
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((20 * density).toInt(), (20 * density).toInt(), (20 * density).toInt(), (20 * density).toInt())
        }

        val etName = createDialogInput("Full Name", tvFullName.text.toString(), InputType.TYPE_CLASS_TEXT, density)
        val etEmail = createDialogInput("Email", tvEmail.text.toString(), InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, density)
        val etPhone = createDialogInput("Phone Number", tvPhone.text.toString(), InputType.TYPE_CLASS_PHONE, density)

        layout.addView(etName)
        layout.addView(etEmail)
        layout.addView(etPhone)

        AlertDialog.Builder(this)
            .setTitle("Edit Profile")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                doUpdateProfile(
                    etName.text.toString(),
                    etEmail.text.toString(),
                    etPhone.text.toString(),
                    null
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showChangePasswordDialog() {
        val density = resources.displayMetrics.density
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((20 * density).toInt(), (20 * density).toInt(), (20 * density).toInt(), (20 * density).toInt())
        }

        val etPassword = createDialogInput("New Password", "", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD, density)

        layout.addView(etPassword)

        AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(layout)
            .setPositiveButton("Change") { _, _ ->
                val newPass = etPassword.text.toString()
                if (newPass.length >= 6) {
                    doUpdateProfile(
                        tvFullName.text.toString(),
                        tvEmail.text.toString(),
                        tvPhone.text.toString(),
                        newPass
                    )
                } else {
                    Toast.makeText(this, "Password minimum 6 characters", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createDialogInput(hintText: String, defaultText: String, type: Int, density: Float): EditText {
        return EditText(this).apply {
            hint = hintText
            setText(defaultText)
            inputType = type
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, (10 * density).toInt())
            }
        }
    }

    private fun loadProfile() {
        lifecycleScope.launch {
            try {
                val res = ApiClient.instance.getProfile(token)
                if (res.isSuccessful && res.body() != null) {
                    val user = res.body()?.user
                    tvFullName.text = user?.fullName ?: "-"
                    tvEmail.text = user?.email ?: "-"
                    tvPhone.text = user?.phone ?: "-"
                } else {
                    Toast.makeText(this@ProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Server connection error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun doUpdateProfile(fullName: String, email: String, phone: String, password: String?) {
        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val request = ProfileUpdateRequest(
            fullName = fullName,
            email = email,
            phone = phone,
            passwordHash = password
        )

        lifecycleScope.launch {
            try {
                val res = ApiClient.instance.updateProfile(token, request)
                if (res.isSuccessful && res.body() != null) {
                    Toast.makeText(this@ProfileActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    tvFullName.text = fullName
                    tvEmail.text = email
                    tvPhone.text = phone
                } else {
                    Toast.makeText(this@ProfileActivity, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
