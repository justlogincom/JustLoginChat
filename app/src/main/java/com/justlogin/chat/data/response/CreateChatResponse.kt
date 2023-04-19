package com.justlogin.chat.data.response

data class CreateChatResponse(
    val messageId: String,
    val messageBody: String,
    val created: String,
    val user: User
)

