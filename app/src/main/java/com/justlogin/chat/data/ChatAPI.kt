package com.justlogin.chat.data

import com.justlogin.chat.common.Consts
import com.justlogin.chat.data.parameter.CreateChatMemberRequest
import com.justlogin.chat.data.parameter.SendMessageRequest
import com.justlogin.chat.data.parameter.UpdateMessageRequest
import com.justlogin.chat.data.parameter.UpdateReadStatusRequest
import com.justlogin.chat.data.response.ChattedUserResponse
import com.justlogin.chat.data.response.CreateChatResponse
import com.justlogin.chat.data.response.LeaveChatResponse
import retrofit2.Response
import retrofit2.http.*

interface ChatAPI {

    @POST("chat/v2/companies/{companyGUID}/members/{transactionId}")
    suspend fun createChatSession(
        @Path("companyGUID") companyGUID: String,
        @Path("transactionId") reportId: String,
        @Body chatMemberRequest: CreateChatMemberRequest
    ): ArrayList<ChattedUserResponse>

    @GET("chat/v1/companies/{companyGUID}/messages/{transactionId}")
    suspend fun getMessages(
        @Path("companyGUID") companyGUID: String,
        @Path("transactionId") reportId: String,
        @Query(Consts.CURRENT_PAGE) currentPage: Int,
        @Query(Consts.NO_OF_RECORDS) noOfRecords: Int
    ): LeaveChatResponse

    @POST("chat/v1/companies/{companyGUID}/messages/{transactionId}")
    suspend fun sendMessages(
        @Path("companyGUID") companyGUID: String,
        @Path("transactionId") reportId: String,
        @Body request: SendMessageRequest
    ): CreateChatResponse

    @PUT("chat/v1/messages/{reportID}")
    suspend fun updateMessage(
        @Path("reportID") reportID: String,
        @Body request: UpdateMessageRequest
    ): Response<Unit>

    @PUT("chat/v1/companies/{companyGUID}/messages")
    suspend fun updateReadMessage(
        @Path("companyGUID") companyGUID: String,
        @Query("transactionid") reportId: String,
        @Body request: UpdateReadStatusRequest
    ): Response<Unit>

    @PUT("chat/v1/messages")
    suspend fun deleteMessage(
        @Query("transactionid") reportId: String,
        @Query("messageid") messageid: String
    ): Response<Unit>


}