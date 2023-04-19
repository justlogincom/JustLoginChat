package com.justlogin.chat.domain

import com.justlogin.chat.data.parameter.UpdateMessageRequest
import com.justlogin.chat.data.repository.ChatRepository

class UpdateMessage(private val repository: ChatRepository) {
    suspend operator fun invoke(
        reportId: String,
        request: UpdateMessageRequest
    ) = repository.updateMessage(reportId, request)

}