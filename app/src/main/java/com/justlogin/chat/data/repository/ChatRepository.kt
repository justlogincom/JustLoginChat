package com.justlogin.chat.data.repository

import com.justlogin.chat.data.parameter.CreateChatMemberRequest
import com.justlogin.chat.data.parameter.SendMessageRequest
import com.justlogin.chat.data.parameter.UpdateMessageRequest
import com.justlogin.chat.data.parameter.UpdateReadStatusRequest
import com.justlogin.chat.data.response.ChattedUserResponse
import com.justlogin.chat.data.response.CreateChatResponse
import com.justlogin.chat.data.response.LeaveChatResponse
import retrofit2.Response

interface ChatRepository {
    suspend fun getMessages(companyGUID: String, reportId: String, currentPage: Int, noOfRecord: Int): LeaveChatResponse
    suspend fun sendMessage(companyGUID: String, reportId: String, request: SendMessageRequest) : CreateChatResponse
    suspend fun updateMessage(reportId: String,request : UpdateMessageRequest): Response<Unit>
    suspend fun updateReadMessage(companyGUID: String,reportId : String,request : UpdateReadStatusRequest): Response<Unit>
    suspend fun deleteMessage(reportId : String,messageId : String): Response<Unit>
    suspend fun initMessaging(companyGUID: String,reportId: String,request : CreateChatMemberRequest) : ArrayList<ChattedUserResponse>
}