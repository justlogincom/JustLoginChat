package com.justlogin.chat.ui.mvi

import com.justlogin.chat.data.parameter.CreateChatMemberRequest
import com.justlogin.chat.data.parameter.SendMessageRequest
import com.justlogin.chat.data.parameter.UpdateMessageRequest
import com.justlogin.chat.data.response.Message


sealed class ChatIntent {
    data class InitialLoad(
        val companyGUID: String,
        val reportId: String,
        val currentPage: Int,
        val noOfPage: Int,
        val request: CreateChatMemberRequest
    ) : ChatIntent()

    data class RefreshPage(
        val companyGUID: String,
        val reportId: String,
        val currentPage: Int,
        val noOfPage: Int,
        val request: CreateChatMemberRequest
    ) : ChatIntent()

    data class SendMessage( val companyGuid: String,
                            val reportId: String, val request: SendMessageRequest) : ChatIntent()

    data class EditMessage(
        val reportId: String,
        val request: UpdateMessageRequest
    ) : ChatIntent()

    data class UpdateMessage(
        val reportId: String,
        val request: UpdateMessageRequest
    ) : ChatIntent()

    data class DeleteMessage(val reportId: String, val messageId: String) : ChatIntent()
}

sealed class ChatAction {
    data class FetchInitialData(
        val companyGUID: String,
        val reportId: String,
        val currentPage: Int,
        val noOfPage: Int,
        val request: CreateChatMemberRequest
    ) : ChatAction()

    data class RefreshData(
        val companyGUID: String,
        val reportId: String,
        val currentPage: Int,
        val noOfPage: Int,
        val request: CreateChatMemberRequest
    ) : ChatAction()

    data class SendMessage(
        val companyGuid: String,
        val reportId: String, val request: SendMessageRequest
    ) : ChatAction()

    data class EditMessage(
        val reportId: String,
        val request: UpdateMessageRequest
    ) : ChatAction()

    data class UpdateMessage(
        val reportId: String,
        val request: UpdateMessageRequest
    ) : ChatAction()

    data class DeleteMessage(val reportId: String, val messageId: String) : ChatAction()
}

sealed class ChatResult {

    sealed class CreateRoom : ChatResult() {
        data class Loading(val loadType: LoadType) : CreateRoom()
        data class Success(val isSuccess: Boolean) : CreateRoom()
        data class Error(val error: Throwable) : CreateRoom()
    }

    sealed class LoadAllUserResult : ChatResult() {
        data class Loading(val loadType: LoadType) : LoadAllUserResult()
        data class Success(val messages: List<Message>, val totalPages: Int) : LoadAllUserResult()
        data class Error(val error: Throwable) : LoadAllUserResult()
    }

    sealed class SendMessage : ChatResult() {
        data class Loading(val loadType: LoadType) : SendMessage()
        data class Success(val success : Boolean) : SendMessage()
        data class Error(val error: Throwable) : SendMessage()
    }

    sealed class DeleteMessage : ChatResult() {
        data class Loading(val loadType: LoadType) : DeleteMessage()
        data class Success(val isSuccess: Boolean) : DeleteMessage()
        data class Error(val error: Throwable,val messageId: String) : DeleteMessage()
    }

    sealed class UpdateMessage : ChatResult() {
        data class Loading(val loadType: LoadType) : UpdateMessage()
        data class Success(val isSuccess: Boolean) : UpdateMessage()
        data class Error(val error: Throwable,val messageId: String) : UpdateMessage()
    }

}

enum class LoadType {
    PULL_TO_REFRESH,
    SHIMMER,
    NONE
}

sealed class ChatViewEffect {
    data class ShowRetrySend(val message: String) : ChatViewEffect()
    data class ShowDeleteAt(val messageId: String) : ChatViewEffect()
    data class ShowFailedFetch(val message : String) : ChatViewEffect()
    data class UpdateMessageAt(val messageId : String) : ChatViewEffect()
}

data class ChatViewState(
    val isInitial: Boolean,
    val messages: List<Message>,
    val loadType: LoadType,
    val error: Throwable?,
) {
    companion object {
        fun initialState() = ChatViewState(
            true, listOf(), LoadType.SHIMMER, null
        )
    }
}
