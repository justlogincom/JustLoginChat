package com.justlogin.chat.data.parameter

data class UpdateMessageRead(
    val messageIds : List<String>,
    val read : Reader
)
