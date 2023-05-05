package com.justlogin.chat.data.response


data class OAuthResponse(
    val token_type: String,
    val access_token: String,
    val expires_in: Long,
    val refresh_token: String,
    var id_token: String,
    var Token_Provider: String
)
