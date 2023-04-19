package com.justlogin.chat.domain

import com.justlogin.chat.data.repository.ChatRepository

class GetMessages(private val repository: ChatRepository) {
    suspend operator fun invoke(
        companyGUID: String,
        reportId: String,
        currentPage: Int,
        noOfPage: Int
    ) = repository.getMessages(
        companyGUID, reportId,
        currentPage, noOfPage
    )
}