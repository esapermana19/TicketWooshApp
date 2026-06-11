package com.esa.ticketwoosh.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // Menggunakan IP khusus 10.0.2.2 untuk menyambungkan emulator ke localhost laptop Anda
    // Atau gunakan IP local laptop Anda jika menggunakan HP Fisik
    private const val BASE_URL = "http://10.10.201.225:8000/api/"

    val instance: ApiService by lazy {
        // Interceptor untuk melihat detail log request data di Logcat Android Studio
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)
    }
}