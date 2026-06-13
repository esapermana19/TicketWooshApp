package com.esa.ticketwoosh.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("woosh_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val USER_TOKEN = "user_token"
        private const val FULL_NAME = "full_name"
    }

    // Fungsi menyimpan token setelah sukses login
    fun saveAuthToken(token: String, fullName: String = "") {
        prefs.edit()
            .putString(USER_TOKEN, token)
            .putString(FULL_NAME, fullName)
            .apply()
    }

    // Fungsi mengambil token untuk request API berikutnya (seperti checkout)
    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    fun getFullName(): String {
        return prefs.getString(FULL_NAME, "User") ?: "User"
    }

    // Fungsi hapus token (Logout)
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}