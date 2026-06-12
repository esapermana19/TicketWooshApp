package com.esa.ticketwoosh.utils

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.esa.ticketwoosh.ui.booking.BookingActivity
import com.esa.ticketwoosh.ui.booking.MyTicketsActivity
import com.esa.ticketwoosh.ui.dashboard.ProfileActivity
import com.esa.ticketwoosh.R

object BottomNavigationUtil {
    /**
     * Setups the bottom navigation menu.
     * @param activity The current activity
     * @param rootLayout The root layout where the bottom navigation should be added
     * @param activeIndex 0 = Home, 1 = My Tickets, 2 = Profile
     * @return The ID of the created BottomNavigation View
     */
    fun setupBottomNav(activity: Activity, rootLayout: RelativeLayout, activeIndex: Int): Int {
        val density = activity.resources.displayMetrics.density
        val wooshRed = Color.parseColor("#ED1C24")

        val bottomNav = LinearLayout(activity).apply {
            id = View.generateViewId()
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.WHITE)
            elevation = 16f * density
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            }
        }

        fun createBottomNavItem(iconResId: Int, label: String, isActive: Boolean, onClick: () -> Unit): LinearLayout {
            return LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
                
                val icon = ImageView(activity).apply {
                    setImageResource(iconResId)
                    setColorFilter(if (isActive) wooshRed else Color.parseColor("#9CA3AF"))
                    layoutParams = LinearLayout.LayoutParams(
                        (24 * density).toInt(),
                        (24 * density).toInt()
                    ).apply {
                        setMargins(0, (8 * density).toInt(), 0, (4 * density).toInt())
                    }
                }
                val text = TextView(activity).apply {
                    text = label
                    textSize = 11f
                    gravity = Gravity.CENTER
                    setTextColor(if (isActive) wooshRed else Color.parseColor("#9CA3AF"))
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 0, 0, (8 * density).toInt())
                    }
                }
                addView(icon)
                addView(text)
                
                setOnClickListener {
                    if (!isActive) onClick()
                }
            }
        }

        bottomNav.addView(createBottomNavItem(R.drawable.ic_home, "Home", activeIndex == 0) {
            val intent = Intent(activity, BookingActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NO_ANIMATION
            activity.startActivity(intent)
            activity.overridePendingTransition(0, 0)
        })
        bottomNav.addView(createBottomNavItem(R.drawable.ic_ticket, "My Tickets", activeIndex == 1) {
            val intent = Intent(activity, MyTicketsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NO_ANIMATION
            activity.startActivity(intent)
            activity.overridePendingTransition(0, 0)
        })
        bottomNav.addView(createBottomNavItem(R.drawable.ic_profil, "Profile", activeIndex == 2) {
            val intent = Intent(activity, ProfileActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NO_ANIMATION
            activity.startActivity(intent)
            activity.overridePendingTransition(0, 0)
        })

        rootLayout.addView(bottomNav)
        return bottomNav.id
    }
}
