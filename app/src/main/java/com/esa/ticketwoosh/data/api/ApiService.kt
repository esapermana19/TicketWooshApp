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
import retrofit2.http.Path
import com.esa.ticketwoosh.data.model.TicketDetailResponse

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

    @Headers("Accept: application/json")
    @POST("payment/checkout")
    suspend fun checkoutPayment(
        @Header("Authorization") token: String,
        @Body request: com.esa.ticketwoosh.data.model.PaymentRequest
    ): Response<PaymentResponse>

    @Headers("Accept: application/json")
    @GET("payment/ticket/{order_id}")
    suspend fun getTicketDetails(
        @Header("Authorization") token: String, // <- Wajib bawa token login
        @Path("order_id") orderId: String       // <- Mengisi {order_id} di URL
    ): Response<TicketDetailResponse>

    @Headers("Accept: application/json")
    @GET("tickets/history")
    suspend fun getTicketHistory(
        @Header("Authorization") token: String
    ): Response<com.esa.ticketwoosh.data.model.TicketHistoryResponse>

    @Headers("Accept: application/json")
    @GET("tickets/history-filtered")
    suspend fun getFilteredTicketHistory(
        @Header("Authorization") token: String,
        @Query("filter") filter: String
    ): Response<com.esa.ticketwoosh.data.model.TicketHistoryResponse>

    @Headers("Accept: application/json")
    @GET("profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<com.esa.ticketwoosh.data.model.ProfileResponse>

    @Headers("Accept: application/json")
    @POST("profile/update")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: com.esa.ticketwoosh.data.model.ProfileUpdateRequest
    ): Response<com.esa.ticketwoosh.data.model.ProfileResponse>
}