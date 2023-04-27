package com.justlogin.chat.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.accompanist.themeadapter.appcompat.AppCompatTheme
import com.justlogin.chat.JLChatSDK
import com.justlogin.chat.R
import com.justlogin.chat.data.parameter.ChatParameter
import com.justlogin.chat.data.parameter.Reader
import com.justlogin.chat.data.parameter.UpdateReadStatusRequest
import com.justlogin.chat.data.parameter.User
import com.justlogin.chat.data.response.Message
import com.justlogin.chat.domain.*
import com.justlogin.chat.module.ViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ChatRoomActivity : ComponentActivity() {

    @Inject
    lateinit var vmFactory: ViewModelFactory

    private val viewModel: ChatRoomViewmodel by viewModels { vmFactory }

    companion object {
        const val PARAM_DATA = "parameter_data"
        private const val DATA_PER_PAGE = 10
    }
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        JLChatSDK.getInstance().component.inject(this)
        val parameterData : ChatParameter? = intent.getParcelableExtra<ChatParameter>(PARAM_DATA)
        var currentPage = 1

        Log.e(
            "Chat SDK",
            "Initialization Chat with \n" +
                    "Token : ${viewModel.getToken()}\n" +
                    "companyId : ${parameterData?.getCompanyId()}\n" +
                    "reportId: ${parameterData?.getRoomId()}\n" +
                    "memberIds: ${parameterData?.getParticipantsIds()?.joinToString()}"
        )

        setContent {
            AppCompatTheme(content = {
                val state = viewModel.uiState.collectAsState()
                LaunchedEffect(key1 = true) {
                    lifecycleScope.launch {
                        repeatOnLifecycle(Lifecycle.State.RESUMED) {
                            viewModel.getAllData(
                                parameterData!!.getCompanyId(),
                                parameterData!!.getRoomId(),
                                currentPage,
                                DATA_PER_PAGE,
                                parameterData!!.getParticipantsIds()
                            )
                        }
                    }
                    lifecycleScope.launch {
                        repeatOnLifecycle(Lifecycle.State.RESUMED) {
                            while (true) {
                                viewModel.updateReadMessage(
                                    parameterData!!.getCompanyId(), parameterData!!.getRoomId(), request = UpdateReadStatusRequest(
                                        messageIds = state.value.messages.map {
                                            it.messageId
                                        }, read = Reader(userGuid = parameterData!!.getUserId(), fullName = parameterData!!.userName)
                                    )
                                )
                                delay(TimeUnit.SECONDS.toMillis(5))
                            }
                        }
                    }
                }

                val listState = rememberLazyListState()
                Scaffold(
                    topBar = {
                        TopAppBar(title = { Text("Messages") })
                    },
                    content = {
                        Column(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
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
                                    parameterData!!.getCompanyId(),
                                    parameterData.getRoomId(),
                                    currentPage++,
                                    DATA_PER_PAGE,
                                    parameterData.getParticipantsIds()
                                )
                            }

                            var rememberText by remember { mutableStateOf("") }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = rememberText,
                                    onValueChange = { rememberText = it },
                                    label = { Text("Message") },
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    modifier = Modifier
                                        .height(50.dp)
                                        .padding(start = 8.dp)
                                        .align(Alignment.CenterVertically),
                                    onClick = {
                                        viewModel.sendMessage(
                                            rememberText, User(parameterData!!.getUserId(), parameterData!!.userName), parameterData!!.getCompanyId(), parameterData!!.getRoomId()
                                        )
                                    },
                                ) {
                                    Image(imageVector = ImageVector.vectorResource(
                                        R.drawable.ic_send_24
                                    ), contentDescription = "")
                                }
                            }
                        }
                    }
                )
            })
        }
    }


    @Preview
    @Composable
    fun ListWithStickyTextFieldAndButton() {
        var textFieldValue by remember { mutableStateOf("") }

        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(5) {
                    Text("Item $it")
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    label = { Text("Message") },
                    modifier = Modifier.weight(1f)
                )
                Button(
                    modifier = Modifier
                        .height(50.dp)
                        .padding(start = 8.dp)
                        .align(Alignment.CenterVertically),
                    onClick = { /* Perform action */ },
                ) {
                    Image(imageVector = ImageVector.vectorResource(
                        R.drawable.ic_send_24
                    ), contentDescription = "")
                }
            }
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

fun Activity.createChatRoomWith(parameter : ChatParameter) = Intent(this,ChatRoomActivity::class.java).apply{
    this.putExtra(ChatRoomActivity.PARAM_DATA,parameter)
}