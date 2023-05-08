package com.justlogin.chat.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
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
import com.google.accompanist.themeadapter.appcompat.AppCompatTheme
import com.justlogin.chat.JLChatSDK
import com.justlogin.chat.R
import com.justlogin.chat.data.parameter.ChatParameter
import com.justlogin.chat.data.parameter.User
import com.justlogin.chat.data.parameter.sanitize
import com.justlogin.chat.data.response.Message
import com.justlogin.chat.module.ViewModelFactory
import com.justlogin.chat.ui.mvi.ChatViewEffect
import com.justlogin.chat.ui.mvi.ErrorType
import com.justlogin.chat.ui.mvi.LoadType
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import javax.inject.Inject

class ChatRoomActivity : ComponentActivity() {

    private val datePattern: String = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

    @Inject
    lateinit var vmFactory: ViewModelFactory

    private val viewModel: ChatRoomViewmodel by viewModels { vmFactory }
    private var currentPage: Int = 1
    private val itemDatas: MutableList<Message> = mutableListOf()
    private val set = HashSet<Message>()

    companion object {
        const val PARAM_DATA = "parameter_data"
        private const val DATA_PER_PAGE = 10
    }

    private var parameterData: ChatParameter? = null


    @SuppressLint("UnusedMaterialScaffoldPaddingParameter", "BinaryOperationInTimber")
    override fun onCreate(savedInstanceState: Bundle?) {
        JLChatSDK.getInstance().component.inject(this)
        super.onCreate(savedInstanceState)
        parameterData = intent.getParcelableExtra(PARAM_DATA)
        Timber.tag("Chat SDK").e(
            "Initialization Chat with \n" +
                    "Token : ${viewModel.getToken()}\n" +
                    "Refresh Token : ${viewModel.getRefreshToken()}\n" +
                    "companyId : ${parameterData?.getCompanyId()}\n" +
                    "reportId: ${parameterData?.getRoomId()}\n" +
                    "memberIds: ${parameterData?.getParticipantsIds()?.joinToString()}"
        )

        setContent {
            AppCompatTheme(content = {

                val scaffoldState = rememberScaffoldState()
                val context = LocalContext.current
                var rememberText by remember { mutableStateOf("") }
                val listState = rememberLazyListState()
                val uiState = viewModel.uiState.collectAsState()
                val coroutineScope = rememberCoroutineScope()
                val isScrolling = remember { mutableStateOf(false) }
                for (message in uiState.value.messages) {
                    if (set.add(message)) {
                        itemDatas.add(message)
                    }
                }

                currentPage = uiState.value.currentPage

                LaunchedEffect(key1 = true, block = {
                    viewModel.startAutoFetching(
                        parameterData!!.getCompanyId(),
                        parameterData!!.getRoomId(),
                        DATA_PER_PAGE,
                        parameterData!!.getParticipantsIds()
                    )
                })

                //initial state get all data
                LaunchedEffect(key1 = true, block = {
                    coroutineScope.launch {
                        requestData(true, currentPage)
                    }
                })

                val viewEffect = viewModel.viewEffect.collectAsState(initial = null)
                when (val effect = viewEffect.value) {
                    ChatViewEffect.RefreshMessageList -> {
                        Timber.e("JLChatSDK refreshing.....")
                        refreshChat(currentPage)
                    }

                    is ChatViewEffect.ShowFailedFetch -> {
                        Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> {}
                }

                when (val error = uiState.value.error) {
                    is ErrorType.CommonError -> {
                        Toast.makeText(context, error.error?.message, Toast.LENGTH_SHORT).show()
                    }

                    is ErrorType.MessageFail -> {
                        if (rememberText.isNotBlank()) {
                            LaunchedEffect(key1 = uiState.value, block = {
                                coroutineScope.launch {
                                    val result = scaffoldState.snackbarHostState.showSnackbar(
                                        message = "Error: ${error.error?.message}",
                                        actionLabel = "Retry",
                                        duration = SnackbarDuration.Indefinite
                                    )

                                    when (result) {
                                        SnackbarResult.Dismissed -> {}
                                        SnackbarResult.ActionPerformed -> {
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
                            })
                        }
                    }

                    else -> {
                        if (error == null && uiState.value.loadType == LoadType.NONE) {
                            rememberText = ""
                        }
                    }
                }


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
                        if (uiState.value.loadType == LoadType.LOAD_MORE) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .wrapContentHeight()
                                    .align(Alignment.CenterHorizontally)
                                    .padding(16.dp),
                            )
                        }

                        //main screen state [INITIAL LOAD STATE, EMPTY STATE, SHOW DATA]
                        when {
                            uiState.value.loadType == LoadType.INITIAL_LOAD -> {
                                showLoading(
                                    Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                            }

                            itemDatas.isEmpty() -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    showNoMessage(
                                        Modifier
                                            .fillMaxWidth()
                                    )
                                }
                            }

                            else -> {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    state = listState,
                                    reverseLayout = true,
                                    contentPadding = PaddingValues(
                                        vertical = 8.dp,
                                        horizontal = 16.dp
                                    )
                                ) {
                                    itemsIndexed(itemDatas.sortedByDescending {
                                        SimpleDateFormat(datePattern).parse(it.created)
                                    }) { _, message ->
                                        ChatBubble(
                                            sender = message.user.fullName,
                                            message = message.messageBody,
                                            isMine = isMine(message),
                                            date = message.created,
                                            isReaded = message.read
                                        )
                                    }
                                }

                                InfiniteListHandler(
                                    listState = listState,
                                    isNextPageAvailable = uiState.value.isNextPageAvailable
                                ) {
                                    requestData(false, uiState.value.nextPage)
                                }
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
                                enabled = rememberText.isNotBlank() && uiState.value.loadType == LoadType.NONE,
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
                                if (uiState.value.loadType == LoadType.SENDING) {
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

    @Composable
    private fun showLoading(modifier: Modifier) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colors.secondary
            )
        }
    }

    @Composable
    fun InfiniteListHandler(
        listState: LazyListState,
        isNextPageAvailable: Boolean,
        buffer: Int = 2,
        onLoadMore: () -> Unit
    ) {
        val loadMore = remember {
            derivedStateOf {
                val layoutInfo = listState.layoutInfo
                val totalItemsNumber = layoutInfo.totalItemsCount
                val lastVisibleItemIndex =
                    (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

                lastVisibleItemIndex > (totalItemsNumber - buffer)
            }
        }

        LaunchedEffect(loadMore) {
            snapshotFlow { loadMore.value }
                .distinctUntilChanged()
                .filter { isNextPageAvailable }
                .collect {
                    onLoadMore()
                }
        }
    }

    @Composable
    private fun showNoMessage(modifier: Modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
        ) {
            Image(
                modifier = Modifier
                    .clipToBounds()
                    .size(108.dp),
                painter = painterResource(id = R.drawable.ic_message_icon),
                contentDescription = "",
                colorFilter = ColorFilter.tint(color = Color.DarkGray)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "No Message",
                fontSize = 12.sp
            )
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

    private fun refreshChat(page: Int) {
        viewModel.refreshChat(
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
                val isEmpty = true
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
                    if (isEmpty) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.weight(1f)
                        ) {
                            showNoMessage(
                                Modifier
                                    .fillMaxWidth()
                            )
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

    private fun isMine(message: Message): Boolean =
        message.user.userGuid.sanitize() == parameterData!!.getUserId().sanitize()

    @Composable
    fun LazyListState.OnBottomReached(
        loadMore: () -> Unit
    ) {
        val shouldLoadMore = remember {
            derivedStateOf {
                val lastVisibleItem =
                    layoutInfo.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf true
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
            horizontalAlignment = if(isMine) Alignment.End else Alignment.Start
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