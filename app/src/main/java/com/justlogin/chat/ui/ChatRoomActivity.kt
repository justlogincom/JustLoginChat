package com.justlogin.chat.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.justlogin.chat.data.parameter.Reader
import com.justlogin.chat.data.parameter.UpdateReadStatusRequest
import com.justlogin.chat.data.parameter.User
import com.justlogin.chat.data.response.Message
import com.justlogin.chat.domain.*
import com.justlogin.chat.module.ViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ChatRoomActivity : ComponentActivity() {

    @Inject
    lateinit var vmFactory: ViewModelFactory

    private val viewModel: ChatRoomViewmodel by viewModels { vmFactory }

    companion object {
        const val COMPANY_GUID = "company_guid_extras"
        const val REPORT_ID = "report_id"
        const val MEMBER_IDS = "member_ids"
        const val USER_ID = "user_id"
        const val USER_FULLNAME = "user_fullname"
        private const val DATA_PER_PAGE = 10
    }

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = intent.extras
        val (userId, userName) = Reader(
            args?.getString(USER_ID, null).orEmpty(),
            args?.getString(USER_FULLNAME, null).orEmpty()
        )
        val companyId = args?.getString(COMPANY_GUID, null).orEmpty()
        val reportId = args?.getString(REPORT_ID, null).orEmpty()
        val memberIds = args?.getStringArrayList(MEMBER_IDS).orEmpty()
        var currentPage = 1

        Timber.e(
            "Initialization Chat with \n" +
                    "Token : ${viewModel.getToken()}\n" +
                    "companyId : $companyId\n" +
                    "reportId: $reportId\n" +
                    "memberIds: $memberIds"
        )

        setContent {

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    viewModel.getAllData(
                        companyId,
                        reportId,
                        currentPage,
                        DATA_PER_PAGE,
                        memberIds
                    )
                }
            }

            val state = viewModel.uiState.collectAsState()
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    while (true) {
                        viewModel.updateReadMessage(
                            companyId, reportId, request = UpdateReadStatusRequest(
                                messageIds = state.value.messages.map {
                                    it.messageId
                                }, read = Reader(userGuid = userId, fullName = userName)
                            )
                        )
                        delay(TimeUnit.SECONDS.toMillis(5))
                    }
                }
            }
            Scaffold(
                topBar = {
                    TopAppBar(title = { Text("Messages") })
                },
                content = {
                    val listState = rememberLazyListState()
                    ConstraintLayout {
                        val (messageList, inputArea, sendButton) = createRefs()

                        LazyColumn(
                            modifier = Modifier.constrainAs(messageList) {
                                top.linkTo(parent.top)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                                bottom.linkTo(inputArea.top)
                            },
                            state = listState,
                            reverseLayout = true,
                            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp)
                        ) {
                            itemsIndexed(state.value.messages) { index, message ->
                                MessageItem(message = message)
                            }
                        }

                        listState.OnBottomReached {
                            viewModel.getAllData(
                                companyId,
                                reportId,
                                currentPage++,
                                DATA_PER_PAGE,
                                memberIds
                            )
                        }

                        var rememberText by remember { mutableStateOf("") }
                        TextField(
                            value = rememberText,
                            onValueChange = { rememberText = it },
                            label = { Text("Send Message") },
                            modifier = Modifier
                                .constrainAs(inputArea) {
                                    bottom.linkTo(parent.bottom)
                                    start.linkTo(parent.start)
                                    end.linkTo(sendButton.start)
                                }
                        )
                        Button(modifier = Modifier
                            .constrainAs(sendButton) {
                                end.linkTo(parent.end)
                                bottom.linkTo(parent.bottom)
                            }
                            .wrapContentWidth(), onClick = {
                            viewModel.sendMessage(
                                rememberText, User("", ""), companyId, reportId
                            )
                        }) {
                            Text(text = "Send")
                        }
                    }
                }
            )
        }
    }

    @Composable
    fun LazyListState.OnBottomReached(
        loadMore: () -> Unit
    ) {
        val shouldLoadMore = remember {
            derivedStateOf {
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
                    ?: return@derivedStateOf true

                lastVisibleItem.index == layoutInfo.totalItemsCount - 1
            }
        }

        // Convert the state into a cold flow and collect
        LaunchedEffect(shouldLoadMore) {
            snapshotFlow { shouldLoadMore.value }
                .collect {
                    // if should load more, then invoke loadMore
                    if (it) loadMore()
                }
        }
    }

    @Composable
    fun MessageItem(message: Message) {
        Text(text = message.messageBody)
    }
}