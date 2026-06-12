package com.esa.ticketwoosh.data.model

import com.google.gson.annotations.SerializedName

data class TicketHistoryResponse(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("data") val data: List<TicketHistoryItem>?,
    @SerializedName("message") val message: String?
)

data class TicketHistoryItem(
    @SerializedName("booking_id") val bookingId: Int,
    @SerializedName("booking_code") val bookingCode: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("schedule") val schedule: HistoryScheduleItem?,
    @SerializedName("payment") val payment: PaymentItem?,
    @SerializedName("passengers") val passengers: List<PassengerItem>?,
    @SerializedName("ticket") val ticket: TicketItem?
)

data class HistoryScheduleItem(
    @SerializedName("schedule_id") val scheduleId: Int?,
    @SerializedName("train_name") val trainName: String?,
    @SerializedName("departure") val departure: DepartureArrival?,
    @SerializedName("arrival") val arrival: DepartureArrival?,
    @SerializedName("price_per_seat") val pricePerSeat: Int?
)

data class DepartureArrival(
    @SerializedName("station_name") val stationName: String?,
    @SerializedName("time") val time: String?
)

data class PaymentItem(
    @SerializedName("payment_id") val paymentId: Int?,
    @SerializedName("method") val method: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("amount") val amount: Int?,
    @SerializedName("date") val date: String?
)

data class PassengerItem(
    @SerializedName("name") val name: String?,
    @SerializedName("id_number") val idNumber: String?,
    @SerializedName("seat") val seat: String?
)

data class TicketItem(
    @SerializedName("ticket_id") val ticketId: Int?,
    @SerializedName("qr_code") val qrCode: String?,
    @SerializedName("status") val status: String?
)
