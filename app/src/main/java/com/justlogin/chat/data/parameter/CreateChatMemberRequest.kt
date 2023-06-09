package com.justlogin.chat.data.parameter

data class CreateChatMemberRequest(
    val notificationKey : String,
    val chatMembers : List<String>,
    val additionalData : Any
)
