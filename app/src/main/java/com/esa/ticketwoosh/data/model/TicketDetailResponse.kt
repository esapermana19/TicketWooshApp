package com.esa.ticketwoosh.data.model

import com.google.gson.annotations.SerializedName

data class TicketDetailResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("data")
    val data: TicketData?
)

data class TicketData(
    @SerializedName("order_id")
    val orderId: String,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("total_amount")
    val totalAmount: Int,
    
    @SerializedName("seat_number")
    val seatNumber: String
)