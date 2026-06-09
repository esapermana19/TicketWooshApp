package com.esa.ticketwoosh.data.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("message") val message: String,
    @SerializedName("token") val token: String?,
    @SerializedName("user") val user: UserData?
)
data class RegisterResponse(
    @SerializedName("message") val message: String,
    @SerializedName("user") val user: UserData?
)

data class UserData(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String
)