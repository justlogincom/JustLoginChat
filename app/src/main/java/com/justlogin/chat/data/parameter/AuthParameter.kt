package com.justlogin.chat.data.parameter

data class AuthParameter(
    val clientID : String,
    val clientSecret: String,
    val token: String,
    val accessToken: String,
    val refreshToken: String
)
