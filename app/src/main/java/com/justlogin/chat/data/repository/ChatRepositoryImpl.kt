package com.justlogin.chat.data.repository

import com.justlogin.chat.data.ChatAPI
import com.justlogin.chat.data.parameter.CreateChatMemberRequest
import com.justlogin.chat.data.parameter.SendMessageRequest
import com.justlogin.chat.data.parameter.UpdateMessageRequest
import com.justlogin.chat.data.parameter.UpdateReadStatusRequest
import com.justlogin.chat.data.response.ChattedUserResponse
import com.justlogin.chat.data.response.CreateChatResponse
import com.justlogin.chat.data.response.LeaveChatResponse
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(private val api: ChatAPI) : ChatRepository {
    override suspend fun getMessages(
        companyGUID: String,
        reportId: String,
        currentPage: Int,
        noOfRecord: Int
    ): LeaveChatResponse = api.getMessages(companyGUID, reportId, currentPage, noOfRecord)


    override suspend fun sendMessage(
        companyGUID: String,
        reportId: String,
        request: SendMessageRequest
    ): CreateChatResponse = api.sendMessages(companyGUID, reportId, request)

    override suspend fun updateMessage(reportId: String, request: UpdateMessageRequest) =
        api.updateMessage(reportId, request)

    override suspend fun updateReadMessage(
        companyGUID: String,
        reportId: String,
        request: UpdateReadStatusRequest
    ) = api.updateReadMessage(companyGUID, reportId, request)

    override suspend fun deleteMessage(reportId: String, messageId: String) =
        api.deleteMessage(reportId, messageId)

    override suspend fun initMessaging(
        companyGUID: String,
        reportId: String,
        request: CreateChatMemberRequest
    ): ArrayList<ChattedUserResponse> =
        api.createChatSession(
            companyGUID,
            reportId,
            request
        )


}