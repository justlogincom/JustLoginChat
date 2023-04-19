package com.justlogin.chat.domain

import com.justlogin.chat.data.parameter.CreateChatMemberRequest
import com.justlogin.chat.data.repository.ChatRepository
import javax.inject.Inject

class CreateRoomChat @Inject constructor(private val repositoryImpl: ChatRepository) {
    suspend operator fun invoke(companyId: String, reportId: String, request: CreateChatMemberRequest) {
        repositoryImpl.initMessaging(companyId, reportId, request)
    }
}