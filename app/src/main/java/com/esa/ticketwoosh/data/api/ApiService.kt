package com.esa.ticketwoosh.data.api

import com.esa.ticketwoosh.data.model.LoginResponse
import com.esa.ticketwoosh.data.model.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {

    @Headers("Accept: application/json")
    @FormUrlEncoded // 1. Beritahu Retrofit untuk mengirimkan data sebagai Form, bukan JSON
    @POST("login")
    suspend fun loginUser(
        @FieldMap fields: HashMap<String, String> // 2. Ubah @Body menjadi @FieldMap
    ): Response<LoginResponse>

    @Headers("Accept: application/json")
    @FormUrlEncoded // 1. Beritahu Retrofit untuk mengirimkan data sebagai Form, bukan JSON
    @POST("register")
    suspend fun registerUser(
        @FieldMap fields: HashMap<String, String> // 2. Ubah @Body menjadi @FieldMap
    ): Response<RegisterResponse>
}