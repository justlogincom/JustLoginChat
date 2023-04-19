package com.justlogin.chat.domain

import com.justlogin.chat.data.parameter.UpdateReadStatusRequest
import com.justlogin.chat.data.repository.ChatRepository

class UpdateReadStatus(private val repository: ChatRepository) {
    suspend operator fun invoke(
        companyId: String,
        reportId: String, request: UpdateReadStatusRequest
    ) = repository.updateReadMessage(companyId, reportId, request)
}