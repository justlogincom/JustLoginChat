package com.justlogin.chat.data.parameter

data class UpdateMessageRequest(
    val MessageId: String,
    val messageBody: String,
    val user: User,
    val reads: List<String>
)

data class User(
    val userGuid: String,
    val fullName: String
)
