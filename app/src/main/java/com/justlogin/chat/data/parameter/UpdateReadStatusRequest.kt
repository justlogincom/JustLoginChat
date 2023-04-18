package com.justlogin.chat.data.parameter

data class UpdateReadStatusRequest(
    val messageIds: List<String>,
    val read: Reader
)

data class Reader(
    val userGuid: String,
    val fullName: String
)
