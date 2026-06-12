package com.esa.ticketwoosh.data.model

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("user") val user: UserProfile?
)

data class UserProfile(
    @SerializedName("user_id") val userId: Int?,
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("phone") val phone: String?
)

data class ProfileUpdateRequest(
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("password_hash") val passwordHash: String?
)
