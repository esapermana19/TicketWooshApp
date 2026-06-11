package com.esa.ticketwoosh.data.model

import com.google.gson.annotations.SerializedName

data class PaymentRequest(
    @SerializedName("booking_id")
    val bookingId: Int?,
    
    @SerializedName("total_price")
    val totalPrice: Int,
    
    @SerializedName("payment_method")
    val paymentMethod: String
)
