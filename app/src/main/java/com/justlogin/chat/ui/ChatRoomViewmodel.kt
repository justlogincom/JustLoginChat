package com.justlogin.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justlogin.chat.data.parameter.CreateChatMemberRequest
import com.justlogin.chat.data.parameter.SendMessageRequest
import com.justlogin.chat.data.parameter.UpdateReadStatusRequest
import com.justlogin.chat.data.parameter.User
import com.justlogin.chat.data.preference.AuthManagement
import com.justlogin.chat.data.response.LeaveChatResponse
import com.justlogin.chat.domain.*
import com.justlogin.chat.module.annnotate.IoDispatcher
import com.justlogin.chat.ui.mvi.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@OptIn(FlowPreview::class)
class ChatRoomViewmodel @Inject constructor(
    val authManagement: AuthManagement,
    invokeCreateRoom: CreateRoomChat,
    invokeDeleteMessage: DeleteMessage,
    invokeGetMessages: GetMessages,
    invokeSendMessage: SendMessage,
    invokeUpdateMessage: UpdateMessage,
    invokeUpdateReadMessages: UpdateReadStatus,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _viewEffect = MutableSharedFlow<ChatViewEffect>()
    val viewEffect = _viewEffect.asSharedFlow()
    private fun viewEffect(result: ChatResult) {
        viewModelScope.launch {
            when (result) {
                is ChatResult.CreateRoom.Error -> {
                    _viewEffect.emit(ChatViewEffect.ShowFailedFetch(message = "Failed to get Conversation"))
                }

                is ChatResult.SendMessage.Error -> {
                    _viewEffect.emit(ChatViewEffect.ShowRetrySend(message = "Failed to send Message"))
                }

                is ChatResult.SendMessage.Success -> {
                    _viewEffect.emit(ChatViewEffect.RefreshMessageList)
                }

                is ChatResult.DeleteMessage.Error -> {
                    _viewEffect.emit(ChatViewEffect.ShowDeleteAt(messageId = ""))
                }

                is ChatResult.UpdateMessage.Error -> {
                    _viewEffect.emit(ChatViewEffect.UpdateMessageAt(messageId = ""))
                }

                else -> {

                }
            }
        }
    }

    private val intentFlow = MutableSharedFlow<ChatIntent>()
    private val intentFilter = { incomingFlow: Flow<ChatIntent> ->
        incomingFlow.shareIn(viewModelScope, SharingStarted.Eagerly)
            .filterIsInstance<ChatIntent>()
    }
    private val _uiState = MutableStateFlow(ChatViewState.initialState())
    val uiState = _uiState.asStateFlow()

    fun getToken() = authManagement.getOauthToken()
    fun getRefreshToken() = authManagement.getRefreshToken()

    fun sendMessage(
        message: String,
        user: User,
        companyGUID: String,
        reportId: String,
    ) {
        viewModelScope.launch {
            intentFlow.emit(
                ChatIntent.SendMessage(
                    companyGUID,
                    reportId,
                    SendMessageRequest(
                        message, user
                    )
                )
            )
        }
    }

    fun getAllData(
        isInitialLoad: Boolean,
        companyGUID: String,
        reportId: String,
        currentPage: Int,
        noOfPage: Int,
        chatMembers: List<String>
    ) {
        viewModelScope.launch {
            intentFlow.emit(
                ChatIntent.InitialLoad(
                    isInitial = isInitialLoad,
                    companyGUID = companyGUID,
                    reportId = reportId,
                    currentPage = currentPage,
                    noOfPage = noOfPage,
                    CreateChatMemberRequest(
                        notificationKey = "ExpenseApprovalChat",
                        chatMembers = chatMembers,
                        additionalData = Any()
                    )
                )
            )
        }
    }

    fun refreshChat(
        companyGUID: String,
        reportId: String,
        currentPage: Int,
        noOfPage: Int,
        chatMembers: List<String>
    ) {
        viewModelScope.launch {
            intentFlow.emit(
                ChatIntent.RefreshPage(
                    companyGUID = companyGUID,
                    reportId = reportId,
                    currentPage = currentPage,
                    noOfPage = noOfPage,
                    CreateChatMemberRequest(
                        notificationKey = "ExpenseApprovalChat",
                        chatMembers = chatMembers,
                        additionalData = Any()
                    )
                )
            )
        }
    }

    private val intentToAction = { flow: Flow<ChatIntent> ->
        flow.map { intent ->
            when (intent) {
                is ChatIntent.ReadMessage -> {
                    ChatAction.ReadMessage(
                        intent.companyGUID,
                        intent.reportId,
                        intent.request
                    )
                }

                is ChatIntent.InitialLoad -> {
                    ChatAction.FetchInitialData(
                        intent.isInitial,
                        intent.companyGUID,
                        intent.reportId,
                        intent.currentPage,
                        intent.noOfPage,
                        intent.request
                    )
                }

                is ChatIntent.RefreshPage -> ChatAction.RefreshData(
                    intent.companyGUID,
                    intent.reportId,
                    intent.currentPage,
                    intent.noOfPage,
                    intent.request
                )

                is ChatIntent.SendMessage -> ChatAction.SendMessage(
                    intent.companyGuid,
                    intent.reportId,
                    intent.request
                )

                is ChatIntent.DeleteMessage -> ChatAction.DeleteMessage(
                    intent.reportId,
                    intent.messageId
                )

                is ChatIntent.EditMessage -> ChatAction.EditMessage(intent.reportId, intent.request)
                is ChatIntent.UpdateMessage -> ChatAction.UpdateMessage(
                    intent.reportId,
                    intent.request
                )
            }
        }.filterIsInstance<ChatAction>()
    }

    private val readMessage = { actionFlow: Flow<ChatAction.ReadMessage> ->
        actionFlow.flatMapConcat { action ->
            flow<ChatResult.ReadMessage> {
                invokeUpdateReadMessages.invoke(
                    action.companyGUID, action.reportId, action.request
                )
                emit(ChatResult.ReadMessage.Success(action.request))
            }.catch { err ->
                emit(ChatResult.ReadMessage.Error(error = err))
            }
        }
    }

    private val createMember = { actionFlow: Flow<ChatAction.FetchInitialData> ->
        actionFlow.flatMapConcat { action ->
            flow<ChatResult.CreateRoom> {
                invokeCreateRoom.invoke(
                    action.companyGUID, action.reportId,
                    action.request
                )
                emit(ChatResult.CreateRoom.Success(true))
            }.catch { err ->
                emit(ChatResult.CreateRoom.Error(error = err))
            }.onStart {
                emit(ChatResult.CreateRoom.Loading(LoadType.INITIAL_LOAD))
            }.flowOn(dispatcher)
        }
    }

    private val fetchMessages = { actionFlow: Flow<ChatAction.FetchInitialData> ->
        actionFlow.flatMapConcat { action ->
            flow<ChatResult.LoadAllUserResult> {
                val result: LeaveChatResponse = invokeGetMessages.invoke(
                    action.companyGUID, action.reportId, action.currentPage, action.noOfPage
                )
                val isNextPageAvailable = action.currentPage < result.totalPages
                val nextPage =
                    if (isNextPageAvailable) action.currentPage + 1 else action.currentPage

                emit(
                    ChatResult.LoadAllUserResult.Success(
                        isInitial = action.isInitial,
                        messages = result.messages,
                        totalPages = result.totalPages,
                        isNextPageAvailable = isNextPageAvailable, nextPage = nextPage,
                        currentPage = action.currentPage
                    )
                )
            }.catch { err ->
                emit(ChatResult.LoadAllUserResult.Error(error = err))
            }.onStart {
                emit(ChatResult.LoadAllUserResult.Loading(LoadType.INITIAL_LOAD))
            }.flowOn(dispatcher)
        }
    }

    private val sendMessage = { actionFlow: Flow<ChatAction.SendMessage> ->
        actionFlow.flatMapConcat { action ->
            flow<ChatResult.SendMessage> {
                invokeSendMessage.invoke(action.companyGuid, action.reportId, action.request)
                emit(ChatResult.SendMessage.Success(true))
            }.catch { err ->
                emit(ChatResult.SendMessage.Error(error = err))
            }.onStart {
                emit(ChatResult.SendMessage.Loading(LoadType.SENDING))
            }.flowOn(dispatcher)
        }
    }

    private val refreshMessage = { actionFlow: Flow<ChatAction.RefreshData> ->
        actionFlow.flatMapConcat { action ->
            flow<ChatResult.RefreshResult> {
                val result: LeaveChatResponse = invokeGetMessages.invoke(
                    action.companyGUID, action.reportId, action.currentPage, action.noOfPage
                )
                val isNextPageAvailable = action.currentPage < result.totalPages
                val nextPage =
                    if (isNextPageAvailable) action.currentPage + 1 else action.currentPage
                emit(
                    ChatResult.RefreshResult.Success(
                        messages = result.messages,
                        totalPages = result.totalPages,
                        isNextPageAvailable = isNextPageAvailable,
                        nextPage = nextPage,
                        currentPage = action.currentPage
                    )
                )
            }.catch { err ->
                emit(ChatResult.RefreshResult.Error(error = err))
            }.flowOn(dispatcher)
        }
    }

    private val deleteMessage = { actionFlow: Flow<ChatAction.DeleteMessage> ->
        actionFlow.flatMapConcat { action ->
            flow<ChatResult.DeleteMessage> {
                invokeDeleteMessage.invoke(
                    action.reportId, action.messageId
                )
                emit(ChatResult.DeleteMessage.Success(true))
            }.catch { err ->
                emit(ChatResult.DeleteMessage.Error(error = err, action.messageId))
            }.onStart {
                emit(ChatResult.DeleteMessage.Loading(LoadType.INITIAL_LOAD))
            }.flowOn(dispatcher)
        }
    }


    private val updateMessage = { actionFlow: Flow<ChatAction.UpdateMessage> ->
        actionFlow.flatMapConcat { action ->
            flow<ChatResult.UpdateMessage> {
                invokeUpdateMessage.invoke(
                    action.reportId, action.request
                )
                emit(ChatResult.UpdateMessage.Success(true))
            }.catch { err ->
                emit(ChatResult.UpdateMessage.Error(error = err, action.request.MessageId))
            }.onStart {
                emit(ChatResult.UpdateMessage.Loading(LoadType.INITIAL_LOAD))
            }.flowOn(dispatcher)
        }
    }

    private val actionProcessor = { incomingFlow: Flow<ChatAction> ->
        val sharedFlow = incomingFlow
            .shareIn(viewModelScope, SharingStarted.Eagerly)

        val fetchInitial = incomingFlow
            .filterIsInstance<ChatAction.FetchInitialData>()
            .shareIn(
                viewModelScope,
                SharingStarted.Eagerly
            )

        val sendMessageAction = sharedFlow
            .filterIsInstance<ChatAction.SendMessage>()
            .shareIn(
                viewModelScope,
                SharingStarted.Eagerly
            )

        merge(
            incomingFlow
                .filterIsInstance<ChatAction.ReadMessage>()
                .let(readMessage),
            fetchInitial.let(createMember),
            fetchInitial.let(fetchMessages),
            sendMessageAction.let(sendMessage),
            sharedFlow
                .filterIsInstance<ChatAction.DeleteMessage>()
                .let(deleteMessage),
            sharedFlow
                .filterIsInstance<ChatAction.UpdateMessage>()
                .let(updateMessage),
            sharedFlow
                .filterIsInstance<ChatAction.RefreshData>()
                .let(refreshMessage)
        ).filterIsInstance<ChatResult>()
    }


    private val reducer = { resultFlow: Flow<ChatResult> ->
        resultFlow.scan(ChatViewState.initialState()) { prevState, result ->
            when (result) {
                is ChatResult.LoadAllUserResult -> {
                    when (result) {
                        is ChatResult.LoadAllUserResult.Error -> {
                            prevState.copy(
                                error = ErrorType.CommonError(result.error),
                                loadType = LoadType.NONE
                            )
                        }

                        is ChatResult.LoadAllUserResult.Loading -> {
                            prevState.copy(
                                error = null,
                                loadType = result.loadType,
                            )
                        }

                        is ChatResult.LoadAllUserResult.Success -> {
                            prevState.copy(
                                error = null,
                                loadType = LoadType.NONE,
                                messages = result.messages,
                                isNextPageAvailable = result.isNextPageAvailable,
                                nextPage = result.nextPage,
                                currentPage = result.currentPage
                            )
                        }
                    }
                }

                is ChatResult.CreateRoom.Error ->
                    prevState.copy(
                        error = ErrorType.CommonError(result.error),
                        loadType = LoadType.NONE,
                    )

                is ChatResult.CreateRoom.Loading ->
                    prevState.copy(
                        error = null,
                        loadType = result.loadType,
                    )

                is ChatResult.CreateRoom.Success ->
                    prevState.copy(
                        error = null,
                        loadType = LoadType.NONE,
                    )

                is ChatResult.DeleteMessage.Error ->
                    prevState.copy(
                        error = ErrorType.CommonError(result.error),
                        loadType = LoadType.NONE,
                    )

                is ChatResult.DeleteMessage.Loading ->
                    prevState.copy(
                        error = null,
                        loadType = LoadType.INITIAL_LOAD,
                    )

                is ChatResult.DeleteMessage.Success ->
                    prevState.copy(
                        error = null,
                        loadType = LoadType.NONE,
                    )

                is ChatResult.SendMessage.Error ->
                    prevState.copy(
                        error = ErrorType.MessageFail(result.error),
                        loadType = LoadType.NONE,
                    )

                is ChatResult.SendMessage.Loading ->
                    prevState.copy(
                        error = null,
                        loadType = LoadType.SENDING,
                    )

                is ChatResult.SendMessage.Success ->
                    prevState.copy(
                        error = null,
                        currentPage = 1, // reset to page 1 when we send new message
                        loadType = LoadType.NONE,
                    )

                is ChatResult.UpdateMessage.Error ->
                    prevState.copy(
                        error = ErrorType.CommonError(result.error),
                        loadType = LoadType.NONE,
                    )

                is ChatResult.UpdateMessage.Loading ->
                    prevState.copy(
                        error = null,
                        loadType = LoadType.INITIAL_LOAD,
                    )

                is ChatResult.UpdateMessage.Success ->
                    prevState.copy(
                        error = null,
                        loadType = LoadType.NONE,
                    )

                is ChatResult.ReadMessage.Error -> prevState.copy(
                    error = ErrorType.CommonError(result.error),
                )

                is ChatResult.ReadMessage.Loading -> prevState.copy(
                    error = null,
                )

                is ChatResult.ReadMessage.Success -> prevState.copy(
                    error = null,
                    readMessageStatusUpdated = result.request.messageIds
                )

                is ChatResult.RefreshResult.Error -> prevState
                is ChatResult.RefreshResult.Loading -> prevState
                is ChatResult.RefreshResult.Success -> prevState.copy(
                    messages = result.messages,
                    currentPage = result.currentPage,
                    isNextPageAvailable = result.isNextPageAvailable
                )
            }
        }
    }


    fun updateReadMessage(companyGUID: String, reportId: String, request: UpdateReadStatusRequest) {
        viewModelScope.launch {
            intentFlow.emit(ChatIntent.ReadMessage(companyGUID, reportId, request))
        }
    }

    override fun onCleared() {
        viewModelScope.cancel()
        super.onCleared()
    }

    fun startAutoFetching(
        companyGUID: String,
        reportId: String,
        noOfPage: Int,
        chatMembers: List<String>
    ) {
        viewModelScope.launch {
            while (true) {
                delay(TimeUnit.SECONDS.toMillis(5L))
                refreshChat(
                    companyGUID,
                    reportId,
                    _uiState.value.currentPage,
                    noOfPage,
                    chatMembers
                )
            }
        }
    }

    init {
        intentFlow
            .let(intentFilter)
            .let(intentToAction)
            .let(actionProcessor)
            .onEach(::viewEffect)
            .let(reducer)
            .onEach { newState ->
                _uiState.value = newState
            }
            .launchIn(viewModelScope)
    }
}