package com.justlogin.chat.data.parameter

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AuthQuery(
    val clientID : String,
    val clientSecret: String,
    val token: String,
    val accessToken: String,
    val refreshToken: String
):Parcelable
