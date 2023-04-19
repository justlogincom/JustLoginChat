package com.justlogin.chat.domain

import com.justlogin.chat.data.repository.ChatRepository

class DeleteMessage(private val repository: ChatRepository) {
    suspend operator fun invoke(reportId: String, messageId: String) {
        repository.deleteMessage(reportId, messageId)
    }
}