package com.esa.ticketwoosh.data.api

import com.esa.ticketwoosh.data.model.LoginResponse
import com.esa.ticketwoosh.data.model.RegisterResponse
import com.esa.ticketwoosh.data.model.StationResponse
import com.esa.ticketwoosh.data.model.ScheduleSearchResponse
import com.esa.ticketwoosh.data.model.SeatResponse
import com.esa.ticketwoosh.data.model.CheckoutRequest
import com.esa.ticketwoosh.data.model.CheckoutResponse
import com.esa.ticketwoosh.data.model.PaymentResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @Headers("Accept: application/json")
    @FormUrlEncoded
    @POST("login")
    suspend fun loginUser(
        @FieldMap fields: HashMap<String, String>
    ): Response<LoginResponse>

    @Headers("Accept: application/json")
    @FormUrlEncoded
    @POST("register")
    suspend fun registerUser(
        @FieldMap fields: HashMap<String, String>
    ): Response<RegisterResponse>

    @GET("stations")
    suspend fun getStations(): Response<List<StationResponse>>

    @GET("schedules/search")
    suspend fun searchSchedules(
        @Query("departure_station") departureStation: Int,
        @Query("arrival_station") arrivalStation: Int,
        @Query("date") date: String
    ): Response<ScheduleSearchResponse>

    /** Ambil semua kursi kereta beserta status booked/available untuk jadwal tertentu */
    @GET("seats")
    suspend fun getSeats(
        @Query("train_id") trainId: Int,
        @Query("schedule_id") scheduleId: Int
    ): Response<SeatResponse>

    /** Kirim booking / checkout baru ke Laravel (membutuhkan Bearer Token) */
    @Headers("Accept: application/json")
    @POST("checkout")
    suspend fun checkout(
        @Header("Authorization") token: String,
        @Body request: CheckoutRequest
    ): Response<CheckoutResponse>

    @FormUrlEncoded
    @POST("payment/checkout")
    suspend fun checkoutPayment(
        @Field("total_price") totalPrice: Int,
        @Field("schedule_id") scheduleId: Int,
        @Field("selected_seats") selectedSeats: String
    ): Response<PaymentResponse>
}