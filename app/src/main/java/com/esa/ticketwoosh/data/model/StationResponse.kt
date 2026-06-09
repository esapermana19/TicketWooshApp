package com.esa.ticketwoosh.data.model

import com.google.gson.annotations.SerializedName

data class StationResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    // Tambahkan field lain di bawah ini JIKA di database Laravel Anda ada kolom lain
    // yang ingin diambil (misal: "city" atau "code"). Jika tidak ada, cukup id dan name saja.
    // @SerializedName("city")
    // val city: String? = null
)