package com.esa.ticketwoosh.data.model

import com.google.gson.annotations.SerializedName

data class PaymentResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("redirect_url") val redirectUrl: String,
    @SerializedName("token") val token: String?
)