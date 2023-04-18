package com.justlogin.chat.ui.mvi

import com.justlogin.chat.data.repository.localdata.Messages


sealed class ChatIntent {
    data class InitialLoad(val companyGUID: String, val usersGUID: List<String>) : ChatIntent()
    data class RefreshPage(val companyGUID: String) : ChatIntent()
    data class SendMessage(val message: String) : ChatIntent()
}

sealed class ChatAction {
    data class FetchInitialData(val companyGUID: String, val usersGUID: List<String>) : ChatAction()
    data class RefreshData(val companyGUID: String) : ChatAction()
    data class SendMessage(val message: String) : ChatAction()
}

sealed class ChatResult {
    sealed class LoadAllUserResult : ChatResult() {
        data class Loading(val loadType: LoadType) : LoadAllUserResult()
        data class Success(val messages: List<Messages>) : LoadAllUserResult()
        data class Error(val error: Throwable) : LoadAllUserResult()
    }
}

enum class LoadType {
    PULL_TO_REFRESH,
    SHIMMER,
    NONE
}

sealed class ChatViewEffect {
    data class ShowSnackBar(val message: String) : ChatViewEffect()
}

data class ChatViewState(
    val isInitial: Boolean,
    val messages: List<Messages>,
    val loadType: LoadType,
    val error: Throwable?,
) {
    companion object {
        fun initialState() = ChatViewState(
            true, listOf(), LoadType.SHIMMER, null
        )
    }
}
