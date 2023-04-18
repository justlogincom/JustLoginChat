package com.justlogin.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justlogin.chat.module.annnotate.IoDispatcher
import com.justlogin.chat.ui.mvi.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class ChatRoomViewmodel @Inject constructor(

    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val intentFlow = MutableSharedFlow<ChatIntent>()
    private val intentFilter = { incomingFlow: Flow<ChatIntent> ->
        incomingFlow.shareIn(viewModelScope, SharingStarted.Eagerly)
            .filterIsInstance<ChatIntent>()
    }
    private val _uiState = MutableStateFlow(ChatViewState.initialState())
    val uiState = _uiState.asStateFlow()

    private val intentToAction = { flow: Flow<ChatIntent> ->
        flow.map { intent ->
            when (intent) {
                is ChatIntent.InitialLoad -> {
                    ChatAction.FetchInitialData(intent.companyGUID, intent.usersGUID)
                }
                is ChatIntent.RefreshPage -> ChatAction.RefreshData(intent.companyGUID)
                is ChatIntent.SendMessage -> ChatAction.SendMessage("")
            }
        }.filterIsInstance<ChatAction>()
    }

    private val createMember = { actionFlow: Flow<ChatAction.FetchInitialData> ->
        actionFlow.flatMapConcat { action ->
            flow<ChatResult.LoadAllUserResult> {
                emit(ChatResult.LoadAllUserResult.Success(listOf()))
            }.catch { err ->
                emit(ChatResult.LoadAllUserResult.Error(error = err))
            }.onStart {
                emit(ChatResult.LoadAllUserResult.Loading(LoadType.SHIMMER))
            }.flowOn(dispatcher)
        }
    }

    private val fetchMessages = { actionFlow: Flow<ChatAction.FetchInitialData> ->
        actionFlow.flatMapConcat { action ->
            flow<ChatResult.LoadAllUserResult> {
                emit(ChatResult.LoadAllUserResult.Success(listOf()))
            }.catch { err ->
                emit(ChatResult.LoadAllUserResult.Error(error = err))
            }.onStart {
                emit(ChatResult.LoadAllUserResult.Loading(LoadType.SHIMMER))
            }.flowOn(dispatcher)
        }
    }

    private val sendMessage = { actionFlow: Flow<ChatAction.SendMessage> ->
        actionFlow.flatMapConcat { action ->
            flow<ChatResult.LoadAllUserResult> {
                emit(ChatResult.LoadAllUserResult.Success(listOf()))
            }.catch { err ->
                emit(ChatResult.LoadAllUserResult.Error(error = err))
            }.onStart {
                emit(ChatResult.LoadAllUserResult.Loading(LoadType.SHIMMER))
            }.flowOn(dispatcher)
        }
    }

    private val refreshMessage = { actionFlow: Flow<ChatAction.RefreshData> ->
        actionFlow.flatMapConcat { action ->
            flow<ChatResult.LoadAllUserResult> {
                emit(ChatResult.LoadAllUserResult.Success(listOf()))
            }.catch { err ->
                emit(ChatResult.LoadAllUserResult.Error(error = err))
            }.onStart {
                emit(ChatResult.LoadAllUserResult.Loading(LoadType.SHIMMER))
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


        val createMemberFlow = fetchInitial.let(createMember)
        val fetchMessagesFlow = fetchInitial.let(fetchMessages)
        val sendMessageFlow = sharedFlow
            .filterIsInstance<ChatAction.SendMessage>()
            .let(sendMessage)
        val refreshMessageFlow =
            sharedFlow
                .filterIsInstance<ChatAction.RefreshData>()
                .let(refreshMessage)
        merge(
            createMemberFlow
                .flatMapConcat { fetchMessagesFlow },
            sendMessageFlow,
            refreshMessageFlow
        ).filterIsInstance<ChatResult>()
    }


    private val reducer = { resultFlow: Flow<ChatResult> ->
        resultFlow.scan(ChatViewState.initialState()) { prevState, result ->
            when (result) {
                is ChatResult.LoadAllUserResult -> {
                    when (result) {
                        is ChatResult.LoadAllUserResult.Error -> {
                            prevState.copy(
                                error = result.error,
                                loadType = LoadType.NONE
                            )
                        }
                        is ChatResult.LoadAllUserResult.Loading -> {
                            prevState.copy(
                                error = null,
                                loadType = result.loadType
                            )
                        }
                        is ChatResult.LoadAllUserResult.Success -> {
                            prevState.copy(
                                error = null,
                                loadType = LoadType.NONE,
                                messages = result.messages
                            )
                        }
                    }
                }
            }
        }
    }

    init {
        intentFlow
            .let(intentFilter)
            .let(intentToAction)
            .let(actionProcessor)
            .let(reducer)
            .onEach { newState ->
                _uiState.value = newState
            }
            .launchIn(viewModelScope)
    }
}