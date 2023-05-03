package com.justlogin.chat.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.justlogin.chat.ui.mvi.ChatViewEffect
import com.justlogin.chat.ui.mvi.ChatViewState
import com.justlogin.chat.ui.mvi.LoadType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.format.DateTimeFormatter
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

    private var parameterData: ChatParameter? = null


    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        JLChatSDK.getInstance().component.inject(this)
        parameterData = intent.getParcelableExtra(PARAM_DATA)
        var currentPage: Int
        val itemDatas: MutableList<Message> = mutableListOf()
        val set = HashSet<Message>()
        Log.e(
            "Chat SDK",
            "Initialization Chat with \n" +
                    "Token : ${parameterData?.getToken()}\n" +
                    "companyId : ${parameterData?.getCompanyId()}\n" +
                    "reportId: ${parameterData?.getRoomId()}\n" +
                    "memberIds: ${parameterData?.getParticipantsIds()?.joinToString()}"
        )

        setContent {
            AppCompatTheme(content = {

                val scaffoldState = rememberScaffoldState()
                val context = LocalContext.current
                var rememberText by remember { mutableStateOf("") }
                var isSending by remember { mutableStateOf(false) }
                val listState = rememberLazyListState()
                val uiState = viewModel.uiState.collectAsState()

                for (message in uiState.value.messages) {
                    if (set.add(message)) {
                        itemDatas.add(message)
                    }
                }

                currentPage = uiState.value.currentPage


                Timber.e("bambang redraw")
                //initial state get all data
                LaunchedEffect(key1 = true, block = {
                    lifecycleScope.launch {
                        Timber.e("bambang initial called")
                        requestData(true, currentPage)
                    }
                })

                LaunchedEffect(key1 = true, block = {
                    lifecycleScope.launch {
                        viewModel.viewEffect.collectLatest {
                            when (it) {
                                is ChatViewEffect.ShowRetrySend -> {
                                    isSending = false
                                    val result = scaffoldState.snackbarHostState.showSnackbar(
                                        message = "Error: ${it.message}",
                                        actionLabel = "Retry",
                                        duration = SnackbarDuration.Short
                                    )
                                    when (result) {
                                        SnackbarResult.Dismissed -> {}
                                        SnackbarResult.ActionPerformed -> {
                                            isSending = true
                                            viewModel.sendMessage(
                                                rememberText,
                                                User(
                                                    parameterData!!.getUserId(),
                                                    parameterData!!.userName
                                                ),
                                                parameterData!!.getCompanyId(),
                                                parameterData!!.getRoomId()
                                            )
                                        }
                                    }
                                }

                                ChatViewEffect.RefreshMessageList -> {
                                    isSending = false
                                    rememberText = ""
                                    requestData(false, currentPage)
                                }

                                is ChatViewEffect.ShowDeleteAt -> {}
                                is ChatViewEffect.ShowFailedFetch -> {
                                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                                }

                                is ChatViewEffect.UpdateMessageAt -> {}
                            }
                        }
                    }
                })
                LaunchedEffect(key1 = true, block = {
                    jobScheduler(uiState, currentPage)
                })

                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        TopAppBar(
                            modifier = Modifier.paint(
                                painterResource(R.drawable.bg_more),
                                contentScale = ContentScale.FillBounds
                            ),
                            elevation = 0.dp,
                            title = { Text(text = "Chat", textAlign = TextAlign.Center) },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    },
                    snackbarHost = { SnackbarHost(hostState = it) }
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (uiState.value.isInitial && uiState.value.loadType != LoadType.NONE) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colors.secondary
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                state = listState,
                                reverseLayout = true,
                                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp)
                            ) {
                                itemsIndexed(itemDatas.sortedByDescending {
                                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(it.created)
                                }) { index, message ->
                                    ChatBubble(
                                        sender = message.user.fullName,
                                        message = message.messageBody,
                                        isMine = isMine(message),
                                        date = message.created,
                                        isReaded = message.read
                                    )
                                }
                            }
                        }

                        listState.OnBottomReached {
                            if (uiState.value.isNextPageAvailable) {
                                requestData(false, uiState.value.nextPage)
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
                                enabled = rememberText.isNotBlank() && !isSending,
                                onClick = {
                                    isSending = true
                                    viewModel.sendMessage(
                                        rememberText,
                                        User(
                                            parameterData!!.getUserId(),
                                            parameterData!!.userName
                                        ),
                                        parameterData!!.getCompanyId(),
                                        parameterData!!.getRoomId()
                                    )
                                },
                            ) {
                                if (isSending) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colors.secondary
                                    )
                                } else {
                                    Image(
                                        imageVector = ImageVector.vectorResource(
                                            R.drawable.ic_send_24
                                        ), contentDescription = ""
                                    )
                                }
                            }
                        }
                    }
                }
            })
        }
    }

    private fun requestData(isInitialLoad: Boolean, page: Int) {
        viewModel.getAllData(
            isInitialLoad,
            parameterData!!.getCompanyId(),
            parameterData!!.getRoomId(),
            page,
            DATA_PER_PAGE,
            parameterData!!.getParticipantsIds()
        )
    }


    @Preview
    @Composable
    fun PreviewIt() {
        val scaffoldState = rememberScaffoldState()
        val listState = rememberLazyListState()

        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                TopAppBar(
                    modifier = Modifier
                        .height(80.dp)
                        .paint(
                            painterResource(id = R.drawable.bg_more),
                            contentScale = ContentScale.Crop,
                            sizeToIntrinsics = true,
                            colorFilter = ColorFilter.tint(
                                MaterialTheme.colors.secondary.copy(0.5f),
                                BlendMode.ColorBurn
                            )
                        ),
                    backgroundColor = Color.Unspecified,
                    elevation = 0.dp,
                    title = { Text(text = "Chat", textAlign = TextAlign.Center) },
                    navigationIcon = {
                        IconButton(onClick = { this@ChatRoomActivity.finishAfterTransition() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState = it) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it.calculateBottomPadding())
            ) {
                val containData = false
                if (containData) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        state = listState,
                        reverseLayout = true,
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp)
                    ) {
                        itemsIndexed(listOf("1", "2").reversed()) { index, message ->
                            ChatBubble(
                                sender = "message.user.fullName",
                                message = "message.messageBody",
                                isMine = true,
                                date = "message.created",
                                isReaded = true
                            )
                        }
                    }
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colors.secondary
                        )
                    }
                }

                listState.OnBottomReached {

                }


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    var rememberText by remember { mutableStateOf("") }
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
                        enabled = rememberText.isNotBlank(),
                        onClick = {
                            viewModel.sendMessage(
                                rememberText,
                                User(
                                    parameterData!!.getUserId(),
                                    parameterData!!.userName
                                ),
                                parameterData!!.getCompanyId(),
                                parameterData!!.getRoomId()
                            )
                        },
                    ) {
                        var showing by remember { mutableStateOf(false) }
                        AnimatedVisibility(
                            modifier = Modifier.size(24.dp),
                            visible = showing,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                        AnimatedVisibility(
                            visible = showing.not(),
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Image(
                                imageVector = ImageVector.vectorResource(
                                    R.drawable.ic_send_24
                                ), contentDescription = ""
                            )
                        }
                    }
                }
            }
        }
    }

    private fun jobScheduler(state: State<ChatViewState>, currentPage: Int) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (true) {
                    val data = state.value.messages.filter {
                        it.user.userGuid != parameterData!!.getUserId()
                    }.map {
                        it.messageId
                    }
                    if (data.isNotEmpty()) {
                        viewModel.updateReadMessage(
                            parameterData!!.getCompanyId(),
                            parameterData!!.getRoomId(),
                            request = UpdateReadStatusRequest(
                                messageIds = data,
                                read = Reader(
                                    userGuid = parameterData!!.getUserId(),
                                    fullName = parameterData!!.userName
                                )
                            )
                        )
                    }
                    requestData(false, currentPage)
                    Timber.e("JLChatSDK: Trying Fetch")
                    delay(TimeUnit.SECONDS.toMillis(5))
                }
            }
        }
    }

    private fun isMine(message: Message): Boolean =
        message.user.userGuid == parameterData!!.getUserId()


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
                    Image(
                        imageVector = ImageVector.vectorResource(
                            R.drawable.ic_send_24
                        ), contentDescription = ""
                    )
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
    fun ChatBubble(
        sender: String,
        message: String,
        isMine: Boolean,
        date: String,
        isReaded: Boolean
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.End
        )
        {
            Card(
                modifier = Modifier.widthIn(max = 340.dp),
                shape = createShape(isMine),
                backgroundColor = if (isMine) MaterialTheme.colors.primary else MaterialTheme.colors.secondary,
            ) {
                Column() {
                    Text(
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 8.dp, top = 8.dp),
                        text = sender,
                        fontSize = 12.sp,
                    )
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = message,
                        fontSize = 16.sp,
                        color = MaterialTheme.colors.onPrimary
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(start = 8.dp, bottom = 4.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = date,
                            fontSize = 10.sp,
                        )
                        if (isReaded) {
                            Readed()
                        } else {
                            Unreaded()
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun Unreaded() {
        Icon(
            painter = painterResource(R.drawable.ic_check_white),
            contentDescription = "",
            modifier = Modifier.size(18.dp),
            tint = Color.White,
        )
    }

    @Composable
    fun Readed() {
        Box() {
            Icon(
                painter = painterResource(R.drawable.ic_check_white),
                contentDescription = "",
                modifier = Modifier.size(18.dp),
                tint = Color.Blue,
            )
            Box(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(18.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_check_white),
                    contentDescription = "",
                    modifier = Modifier.size(18.dp),
                    tint = Color.Blue,
                )
            }
        }
    }

    private fun createShape(mine: Boolean) = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomEnd = if (mine) 0.dp else 16.dp,
        bottomStart = if (mine) 16.dp else 0.dp
    )

}

fun Activity.createChatRoomWith(parameter: ChatParameter) =
    Intent(this, ChatRoomActivity::class.java).apply {
        this.putExtra(ChatRoomActivity.PARAM_DATA, parameter)
    }