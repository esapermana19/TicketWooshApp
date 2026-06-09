package com.esa.ticketwoosh.data.model

import com.google.gson.annotations.SerializedName

data class ScheduleSearchResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<ScheduleItem>
)

data class ScheduleItem(
    @SerializedName("schedule_id")
    val scheduleId: Int,
    @SerializedName("train_id")
    val trainId: Int,
    @SerializedName("departure_station")
    val departureStation: StationItem,
    @SerializedName("arrival_station")
    val arrivalStation: StationItem,
    @SerializedName("departure_time")
    val departureTime: String,
    @SerializedName("arrival_time")
    val arrivalTime: String,
    @SerializedName("price")
    val price: String,
    @SerializedName("train")
    val train: TrainItem,
    /** Jumlah kursi tersedia dari API. null = field belum ada di response (Gson tidak set default Kotlin). */
    @SerializedName("available_seats")
    val availableSeats: Int? = null
)

data class StationItem(
    @SerializedName("station_id")
    val stationId: Int,
    @SerializedName("station_name")
    val stationName: String,
    @SerializedName("city")
    val city: String,
    @SerializedName("code")
    val code: String
)

data class TrainItem(
    @SerializedName("train_id")
    val trainId: Int,
    @SerializedName("train_name")
    val trainName: String,
    @SerializedName("train_code")
    val trainCode: String,
    @SerializedName("total_seats")
    val totalSeats: Int
)
