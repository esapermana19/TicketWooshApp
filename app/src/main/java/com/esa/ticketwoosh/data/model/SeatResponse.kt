package com.esa.ticketwoosh.data.model

import com.google.gson.annotations.SerializedName

data class SeatResponse(
    @SerializedName("seats")
    val seats: List<SeatApiItem>
)

data class SeatApiItem(
    @SerializedName("seat_id")
    val seatId: Int,
    @SerializedName("train_id")
    val trainId: Int,
    @SerializedName("seat_number")
    val seatNumber: String,   // format: "G1-1A", "G2-5C", etc.
    @SerializedName("class")
    val seatClass: String,    // "vip", "business", "economy"
    @SerializedName("is_booked")
    val isBooked: Boolean = false
)

data class CheckoutRequest(
    @SerializedName("schedule_id") val scheduleId: Int,
    @SerializedName("payment_method") val paymentMethod: String,
    @SerializedName("passengers") val passengers: List<CheckoutPassenger>
)

data class CheckoutPassenger(
    @SerializedName("full_name") val fullName: String,
    @SerializedName("id_number") val idNumber: String,
    @SerializedName("seat_id") val seatId: Int
)

data class CheckoutResponse(
    @SerializedName("message") val message: String,
    @SerializedName("booking_code") val bookingCode: String?,
    @SerializedName("total_bayar") val totalBayar: Double?,
    @SerializedName("metode") val metode: String?,
    @SerializedName("batas_waktu") val batasWaktu: String?
)
