package com.justlogin.chat.data

import com.justlogin.chat.common.Consts
import com.justlogin.chat.data.parameter.CreateChatMemberRequest
import com.justlogin.chat.data.parameter.UpdateReadStatusRequest
import com.justlogin.chat.data.response.ChattedUserResponse
import com.justlogin.chat.data.response.CreateChatResponse
import com.justlogin.chat.data.response.LeaveChatResponse
import retrofit2.Response
import retrofit2.http.*

interface ChatAPI {

    @POST("chat/v2/companies/{companyGUID}/members/{transactionId}")
    fun createChatSession(
        @Header(Consts.AUTHORIZATION) accessToken: String,
        @Path("companyGUID") companyGUID: String,
        @Path("transactionId") transactionId: String,
        @Body chatMemberRequest: CreateChatMemberRequest
    ): ArrayList<ChattedUserResponse>

    @GET("chat/v1/companies/{companyGUID}/messages/{transactionId}")
    fun getLeaveChats(
        @Header(Consts.AUTHORIZATION) accessToken: String,
        @Path("companyGUID") companyGUID: String,
        @Path("transactionId") transactionId: String,
        @Query(Consts.CURRENT_PAGE) currentPage: Int,
        @Query(Consts.NO_OF_RECORDS) noOfRecords: Int
    ): LeaveChatResponse

    @POST("/v1/companies/{companyGUID}/messages/{transactionId}")
    fun createLeaveChat(
        @Header(Consts.AUTHORIZATION) accessToken: String,
        @Body request: CreateChatMemberRequest
    ): CreateChatResponse

    @PUT
    fun updateReadStatus(
        @Url url: String,
        @Header(Consts.AUTHORIZATION) accessToken: String,
        @Query(Consts.TRANSACTION_ID) transactionId: String,
        @Body request: UpdateReadStatusRequest
    ): Response<Unit>

}