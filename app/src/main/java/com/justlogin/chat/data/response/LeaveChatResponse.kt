package com.justlogin.chat.data.response

import java.text.SimpleDateFormat

data class LeaveChatResponse(
    val transactionId: String,
    val pageUrl: String,
    val totalPages: Int,
    val messages: List<Message>
)

data class Message(
    val messageId: String,
    val messageBody: String,
    val read: Boolean,
    val created: String,
    val user: User,
    val reads: List<Any>,
    var showImage: Boolean = false,
    var isDifferent : Boolean = false,
)

data class User(
    val userGuid: String,
    val fullName: String,
    val profileUrl: String
)