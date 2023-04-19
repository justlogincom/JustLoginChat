package com.justlogin.chat.domain

import com.justlogin.chat.data.parameter.SendMessageRequest
import com.justlogin.chat.data.repository.ChatRepository

class SendMessage(private val repository: ChatRepository) {
    suspend operator fun invoke(
        companyGuid: String,
        reportId: String, request: SendMessageRequest
    ) = repository.sendMessage(companyGuid, reportId, request)

}