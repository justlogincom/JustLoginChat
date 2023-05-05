package com.justlogin.chat.ui.mvi

import com.justlogin.chat.data.parameter.CreateChatMemberRequest
import com.justlogin.chat.data.parameter.SendMessageRequest
import com.justlogin.chat.data.parameter.UpdateMessageRequest
import com.justlogin.chat.data.parameter.UpdateReadStatusRequest
import com.justlogin.chat.data.response.Message


sealed class ChatIntent {
    data class InitialLoad(
        val isInitial: Boolean,
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

    data class SendMessage(
        val companyGuid: String,
        val reportId: String, val request: SendMessageRequest
    ) : ChatIntent()

    data class EditMessage(
        val reportId: String,
        val request: UpdateMessageRequest
    ) : ChatIntent()

    data class UpdateMessage(
        val reportId: String,
        val request: UpdateMessageRequest
    ) : ChatIntent()

    data class ReadMessage(
        val companyGUID: String,
        val reportId: String,
        val request: UpdateReadStatusRequest
    ) : ChatIntent()

    data class DeleteMessage(val reportId: String, val messageId: String) : ChatIntent()
}

sealed class ChatAction {
    data class ReadMessage(
        val companyGUID: String,
        val reportId: String,
        val request: UpdateReadStatusRequest
    ) : ChatAction()

    data class FetchInitialData(
        val isInitial: Boolean,
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

    sealed class ReadMessage : ChatResult() {
        data class Loading(val loadType: LoadType) : ReadMessage()
        data class Success(val request: UpdateReadStatusRequest) : ReadMessage()
        data class Error(val error: Throwable) : ReadMessage()
    }

    sealed class LoadAllUserResult : ChatResult() {
        data class Loading(val loadType: LoadType) : LoadAllUserResult()
        data class Success(
            val currentPage: Int,
            val messages: List<Message>,
            val totalPages: Int,
            val isNextPageAvailable: Boolean,
            val nextPage: Int
        ) : LoadAllUserResult()

        data class Error(val error: Throwable) : LoadAllUserResult()
    }

    sealed class RefreshResult : ChatResult() {
        data class Loading(val loadType: LoadType) : RefreshResult()
        data class Success(
            val currentPage: Int,
            val messages: List<Message>,
            val totalPages: Int,
            val isNextPageAvailable: Boolean,
            val nextPage: Int
        ) : RefreshResult()

        data class Error(val error: Throwable) : RefreshResult()
    }

    sealed class SendMessage : ChatResult() {
        data class Loading(val loadType: LoadType) : SendMessage()
        data class Success(val success: Boolean) : SendMessage()
        data class Error(val error: Throwable) : SendMessage()
    }

    sealed class DeleteMessage : ChatResult() {
        data class Loading(val loadType: LoadType) : DeleteMessage()
        data class Success(val isSuccess: Boolean) : DeleteMessage()
        data class Error(val error: Throwable, val messageId: String) : DeleteMessage()
    }

    sealed class UpdateMessage : ChatResult() {
        data class Loading(val loadType: LoadType) : UpdateMessage()
        data class Success(val isSuccess: Boolean) : UpdateMessage()
        data class Error(val error: Throwable, val messageId: String) : UpdateMessage()
    }

}

enum class LoadType {
    NONE,
    LOAD_MORE,
    INITIAL_LOAD,
    SENDING,
}

sealed class ChatViewEffect {
    object RefreshMessageList : ChatViewEffect()
    data class ShowRetrySend(val message: String) : ChatViewEffect()
    data class ShowDeleteAt(val messageId: String) : ChatViewEffect()
    data class ShowFailedFetch(val message: String) : ChatViewEffect()
    data class UpdateMessageAt(val messageId: String) : ChatViewEffect()
}

sealed class ErrorType {
    data class MessageFail(val error: Throwable?) : ErrorType()
    data class CommonError(val error: Throwable?): ErrorType()
}

data class ChatViewState(
    val messages: List<Message>,
    val loadType: LoadType,
    val nextPage: Int,
    val currentPage: Int,
    val isNextPageAvailable: Boolean,
    val readMessageStatusUpdated: List<String>,
    val error: ErrorType?,
) {
    companion object {
        fun initialState() = ChatViewState(
            messages = listOf(),
            loadType = LoadType.INITIAL_LOAD,
            nextPage = 1,
            currentPage = 1,
            isNextPageAvailable = false,
            readMessageStatusUpdated = listOf(),
            error = null
        )
    }
}
