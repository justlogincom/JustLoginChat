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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import com.google.accompanist.themeadapter.appcompat.AppCompatTheme
import com.justlogin.chat.JLChatSDK
import com.justlogin.chat.R
import com.justlogin.chat.common.parse
import com.justlogin.chat.data.parameter.ChatParameter
import com.justlogin.chat.data.parameter.ClientType
import com.justlogin.chat.data.parameter.Reader
import com.justlogin.chat.data.parameter.UpdateReadStatusRequest
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
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import javax.inject.Inject

class ChatRoomActivity : ComponentActivity() {


    private val idsSet = HashSet<String>()

    private val list: List<Pair<String, Map<String, List<Message>>>> = listOf(
        "2023-05-05T08:34:28.929Z" to mapOf(
            "Dony1" to listOf(
                Message(
                    "", "woy1", false, "2021", com.justlogin.chat.data.response.User(
                        "321",
                        "Dony Nuransyah",
                        "https://images.squarespace-cdn.com/content/v1/5af1298bfcf7fd60f31f66bd/49bf2cc5-d125-4964-a734-c42ec47e64d5/AVATAR+THE+LAST+AIRBENDER.png?format=256w"
                    ),
                    listOf()
                ),
                Message(
                    "", "woy1", false, "2021", com.justlogin.chat.data.response.User(
                        "321",
                        "Dony Nuransyah",
                        "https://images.squarespace-cdn.com/content/v1/5af1298bfcf7fd60f31f66bd/49bf2cc5-d125-4964-a734-c42ec47e64d5/AVATAR+THE+LAST+AIRBENDER.png?format=256w"
                    ),
                    listOf()
                )
            ),
            "Dony2" to listOf(
                Message(
                    "", "woy2", false, "2021", com.justlogin.chat.data.response.User(
                        "123",
                        "Dony Nuransyah",
                        "https://images.squarespace-cdn.com/content/v1/5af1298bfcf7fd60f31f66bd/49bf2cc5-d125-4964-a734-c42ec47e64d5/AVATAR+THE+LAST+AIRBENDER.png?format=256w"
                    ),
                    listOf()
                ),
                Message(
                    "", "woy2", false, "2021", com.justlogin.chat.data.response.User(
                        "123",
                        "Dony Nuransyah",
                        "https://images.squarespace-cdn.com/content/v1/5af1298bfcf7fd60f31f66bd/49bf2cc5-d125-4964-a734-c42ec47e64d5/AVATAR+THE+LAST+AIRBENDER.png?format=256w"
                    ),
                    listOf()
                ),
                Message(
                    "", "woy2", false, "2021", com.justlogin.chat.data.response.User(
                        "123",
                        "Dony Nuransyah",
                        "https://images.squarespace-cdn.com/content/v1/5af1298bfcf7fd60f31f66bd/49bf2cc5-d125-4964-a734-c42ec47e64d5/AVATAR+THE+LAST+AIRBENDER.png?format=256w"
                    ),
                    listOf()
                )
            )
        ),
        "2023-05-15T08:34:28.929Z" to mapOf(
            "Dony2" to listOf(
                Message(
                    "", "woy2", false, "2021", com.justlogin.chat.data.response.User(
                        "123",
                        "Dony Nuransyah",
                        "https://images.squarespace-cdn.com/content/v1/5af1298bfcf7fd60f31f66bd/49bf2cc5-d125-4964-a734-c42ec47e64d5/AVATAR+THE+LAST+AIRBENDER.png?format=256w"
                    ),
                    listOf()
                )
            )
        ),
        "2023-05-08T08:34:28.929Z" to mapOf(
            "Dony3" to listOf(
                Message(
                    "", "woy3", false, "2021", com.justlogin.chat.data.response.User(
                        "321",
                        "Dony Nuransyah",
                        "https://images.squarespace-cdn.com/content/v1/5af1298bfcf7fd60f31f66bd/49bf2cc5-d125-4964-a734-c42ec47e64d5/AVATAR+THE+LAST+AIRBENDER.png?format=256w"
                    ),
                    listOf()
                )
            )
        ),
        "2023-05-04T08:34:28.929Z" to mapOf(
            "Dony4" to listOf(
                Message(
                    "", "woy4", false, "2021", com.justlogin.chat.data.response.User(
                        "321",
                        "Dony Nuransyah",
                        "https://images.squarespace-cdn.com/content/v1/5af1298bfcf7fd60f31f66bd/49bf2cc5-d125-4964-a734-c42ec47e64d5/AVATAR+THE+LAST+AIRBENDER.png?format=256w"
                    ),
                    listOf()
                )
            )
        )
    )

    @Inject
    lateinit var vmFactory: ViewModelFactory

    private val viewModel: ChatRoomViewmodel by viewModels { vmFactory }
    private var currentPage: Int = 1
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
        val isExpense = JLChatSDK.getInstance().clientType == ClientType.Expense
        Timber.tag("Chat SDK").e(
            "Initialization Chat with \n" +
                    "Token : ${viewModel.getToken()}\n" +
                    "Refresh Token : ${viewModel.getRefreshToken()}\n" +
                    "companyId : ${parameterData?.getCompanyId()}\n" +
                    "reportId: ${parameterData?.getRoomId()}\n" +
                    "memberIds: ${parameterData?.getParticipantsIds()?.joinToString()}"
        )

        setContent {
            var isInitial: Boolean by remember {
                mutableStateOf(true)
            }
            AppCompatTheme(content = {

                val scaffoldState = rememberScaffoldState()
                val context = LocalContext.current
                var rememberText by remember { mutableStateOf("") }
                val listState = rememberLazyListState()
                val uiState = viewModel.uiState.collectAsState()
                val coroutineScope = rememberCoroutineScope()
                var isFirstMessage = remember { mutableStateOf(false) }
                var toolBarHeight = remember { mutableStateOf(0) }
                var inputHeight = remember { mutableStateOf(0) }

                currentPage = uiState.value.currentPage
                isFirstMessage.value = uiState.value.messages.map {
                    it.second
                }.flatten().let {
                    it.size < 8
                }

                LaunchedEffect(key1 = true, block = {
                    viewModel.startAutoFetching(
                        parameterData!!.getCompanyId(),
                        parameterData!!.getRoomId(),
                        DATA_PER_PAGE,
                        parameterData!!.getParticipantsIds()
                    )
                })

                LaunchedEffect(key1 = true, block = {
                    viewModel.startAutoFetchingRead(
                        parameterData!!.getCompanyId(),
                        parameterData!!.getRoomId(),
                        parameterData!!.getUserId(),
                        parameterData!!.userName
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
                        Timber.e("JLChatSDK state = refreshing after send.....")
                        refreshChat(currentPage)
                    }

                    ChatViewEffect.GetInitialMessage -> {
                        if (isInitial) {
                            isInitial = false
                            Timber.e("JLChatSDK state = get initial message at $currentPage .....")
                            requestMessage(true, currentPage)
                        }
                    }

                    ChatViewEffect.RefreshReadStatus -> {

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
                            modifier = Modifier
                                .height(60.dp)
                                .paint(
                                    painterResource(if (isExpense) R.drawable.bg_more_expense else R.drawable.bg_more),
                                    contentScale = ContentScale.Crop,
                                    sizeToIntrinsics = true,
                                )
                                .onGloballyPositioned { coordinates ->
                                    toolBarHeight.value = coordinates.size.height
                                },
                            backgroundColor = Color.Unspecified,
                            elevation = 0.dp,
                            title = {
                                Text(
                                    text = "Chat",
                                    modifier = Modifier
                                        .wrapContentHeight()
                                        .fillMaxWidth(0.8f),
                                    textAlign = TextAlign.Center
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    },
                    snackbarHost = { SnackbarHost(hostState = it) }
                ) { parentPadding ->
                    ConstraintLayout(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = parentPadding.calculateTopPadding())
                    ) {
                        val (items, firstLoad, placeholder, loadMore, input) = createRefs()
                        if (uiState.value.loadType == LoadType.LOAD_MORE) {
                            Column(
                                Modifier
                                    .wrapContentWidth()
                                    .wrapContentHeight()
                                    .constrainAs(loadMore) {
                                        top.linkTo(parent.top)
                                        start.linkTo(parent.start)
                                        end.linkTo(parent.end)
                                    }) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .padding(16.dp),
                                )
                            }
                        }

                        //main screen state [INITIAL LOAD STATE, EMPTY STATE, SHOW DATA]
                        when {
                            uiState.value.loadType == LoadType.INITIAL_LOAD -> {
                                showLoading(
                                    Modifier
                                        .constrainAs(firstLoad) {
                                            top.linkTo(parent.top)
                                            start.linkTo(parent.start)
                                            end.linkTo(parent.end)
                                            bottom.linkTo(parent.bottom)
                                        })
                            }

                            uiState.value.messages.isEmpty() -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.constrainAs(placeholder) {
                                        top.linkTo(parent.top)
                                        start.linkTo(parent.start)
                                        end.linkTo(parent.end)
                                        bottom.linkTo(input.top)
                                    }
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
                                        .apply {
                                            if (isFirstMessage.value) {
                                                wrapContentHeight(Alignment.Top)
                                            }
                                        }
                                        .constrainAs(items) {
                                            if (isFirstMessage.value) {
                                                top.linkTo(parent.top)
                                                start.linkTo(parent.start)
                                                end.linkTo(parent.end)
                                            } else {
                                                top.linkTo(parent.top)
                                                start.linkTo(parent.start)
                                                end.linkTo(parent.end)
                                                bottom.linkTo(input.top)
                                            }
                                        }
                                        .padding(
                                            top = 40.dp,
                                            bottom = 25.dp
                                        ),
                                    state = listState,
                                    reverseLayout = true,
                                    contentPadding = PaddingValues(
                                        vertical = 8.dp,
                                        horizontal = 16.dp
                                    )
                                ) {

                                    itemsIndexed(uiState.value.messages.reversed()) { _, message ->
                                        ChatBubble(messages = message)
                                    }
                                }

                                InfiniteListHandler(
                                    listState = listState,
                                    isNextPageAvailable = uiState.value.isNextPageAvailable
                                ) {
                                    requestMessage(false, uiState.value.nextPage)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .constrainAs(input) {
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                    bottom.linkTo(parent.bottom)
                                }
                                .padding(8.dp)
                                .onGloballyPositioned { component ->
                                    inputHeight.value = component.size.height
                                },
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

    private fun updateRead(list: List<String>) {
        viewModel.updateReadMessage(
            parameterData!!.getCompanyId(),
            parameterData!!.getRoomId(),
            UpdateReadStatusRequest(
                list,
                read = Reader(parameterData!!.getUserId(), parameterData!!.userName)
            )
        )
    }

    private val formatter = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    private val formatterMillis = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

    private fun String.toMillis(): Long {
        val dateFormat = try {
            SimpleDateFormat(formatterMillis).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.parse(this).time
        } catch (ex: Exception) {
            SimpleDateFormat(formatter).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.parse(this).time
        }
        return dateFormat
    }

    @Composable
    private fun ChatBubbleWithAva(_message: Pair<String, Map<String, List<Message>>>) {
        Column {
            Text(text = _message.first)
            _message.second.forEach {
                Text(text = it.key)
                it.value.forEach { message ->
                }
            }
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

    private fun requestMessage(isInitialLoad: Boolean, page: Int) {
        viewModel.getAllMessage(
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
                            painterResource(id = R.drawable.bg_more_expense),
                            contentScale = ContentScale.Crop,
                            sizeToIntrinsics = true
                        ),
                    backgroundColor = Color.Unspecified,
                    elevation = 0.dp,
                    title = {
                        Text(
                            text = "Chat",
                            modifier = Modifier
                                .wrapContentHeight()
                                .fillMaxWidth(0.8f),
                            textAlign = TextAlign.Center
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { this@ChatRoomActivity.finishAfterTransition() }) {
                            Row() {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                            }
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
                val containData = true
                val isEmpty = false
                if (containData) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        reverseLayout = true,
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp)
                    ) {
                        itemsIndexed(list.reversed()) { _, message ->
//                            ChatBubble(messages = message)
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

    private fun Message.isMine(): Boolean =
        this.user.userGuid.sanitize().equals(parameterData!!.getUserId().sanitize(), true)

    private fun Message.isDifferentUser(userGuid: String): Boolean =
        this.user.userGuid.sanitize().equals(userGuid.sanitize(), true).not()

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


    private fun Long.getDateFormated(): String? {
        val currentTimeMillis = System.currentTimeMillis()

        val yesterdayCalendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val date = Date(this)

        val format = SimpleDateFormat("hh:mm a")
        format.timeZone = TimeZone.getDefault()

        val formattedDate = when {
            isSameDay(this, currentTimeMillis) -> "Today, ${format.format(Date(this))}"
            isSameDay(
                this,
                yesterdayCalendar.timeInMillis
            ) -> "Yesterday, ${format.format(date)}"

            else -> SimpleDateFormat("EEE, dd MMM yyyy, hh:mm a").format(date)
        }
        return formattedDate
    }

    private fun isSameDay(timeMillis1: Long, timeMillis2: Long): Boolean {
        val calendar1 = Calendar.getInstance()
        val calendar2 = Calendar.getInstance()
        calendar1.timeInMillis = timeMillis1
        calendar2.timeInMillis = timeMillis2

        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
                && calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH)
                && calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH)
    }

    @Composable
    fun ChatBubble(messages: Pair<String, List<Message>>) {
        var userId by remember {
            mutableStateOf("")
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
        {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(start = 8.dp, top = 8.dp),
                text = messages.second.first().created.toMillis().getDateFormated()
                    .toString(),
                fontSize = 12.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            messages.second.forEach { message ->
                Column(
                    modifier = Modifier.align(
                        if (message.isMine()) Alignment.End else Alignment.Start
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (message.isMine()) {
                            userId = ""
                            my(message)
                        } else {
                            populate(message.messageId)
                            Column() {
                                val differentSender = userId != message.user.userGuid
                                if (differentSender) {
                                    userId = message.user.userGuid
                                    Text(
                                        modifier = Modifier
                                            .padding(start = 38.dp, top = 8.dp)
                                            .align(if (message.isMine()) Alignment.End else Alignment.Start),
                                        text = message.user.fullName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                other(message)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    private fun populate(messageId: String) {
        if (idsSet.add(messageId)) {
            viewModel.idsMessage.add(messageId)
        }
    }

    @Composable
    @Preview
    fun ShowMyMessage() {
        my(
            message = Message(
                "2",
                LoremIpsum(50).values.first(),
                false,
                "",
                com.justlogin.chat.data.response.User(
                    "dsa",
                    "Dony",
                    "https://w7.pngwing.com/pngs/340/946/png-transparent-avatar-user-computer-icons-software-developer-avatar-child-face-heroes-thumbnail.png"
                ),
                listOf(),
                false,
                false
            )
        )
    }

    @Composable
    private fun my(message: Message) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Row(modifier = Modifier.align(Alignment.End)) {
                Row(modifier = Modifier.wrapContentSize()) {
                    if (message.read) {
                        Readed()
                    } else {
                        Unreaded()
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Message(message = message.messageBody, true)
            }
        }
    }

    @Composable
    private fun other(message: Message) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Row(modifier = Modifier.align(Alignment.Start)) {
                if (message.showImage) {
                    CircularAvatar(
                        url = "${JLChatSDK.getInstance().IMAGE_URL}/size-128/${message.user.userGuid}"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Spacer(modifier = Modifier.width(35.dp))
                }
                Message(message = message.messageBody, false)
            }
        }
    }

    @Composable
    private fun Message(message: String, sender: Boolean) {
        val color = if (sender) {
            Color.parse(JLChatSDK.getInstance().clientType.senderChatBackground)
        } else {
            Color.parse(JLChatSDK.getInstance().clientType.receiverChatBackground)
        }
        Card(
            modifier = Modifier.widthIn(max = 340.dp),
            elevation = 0.dp,
            shape = RoundedCornerShape(10.dp),
            backgroundColor = color,
        ) {
            Column() {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = message,
                    fontSize = 16.sp,
                    color = Color.DarkGray
                )
            }
        }
    }

    @Composable
    fun Unreaded() {
        Icon(
            painter = painterResource(R.drawable.ic_check_white),
            contentDescription = "",
            modifier = Modifier.size(18.dp),
            tint = Color.Gray,
        )
    }

    @Composable
    fun Readed() {
        Box(modifier = Modifier.wrapContentSize()) {
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


    @OptIn(ExperimentalCoilApi::class)
    @Composable
    fun CircularAvatar(url: String) {
        var isLoading by remember { mutableStateOf(true) }

        Box(
            modifier = Modifier
                .size(32.dp) // Set the desired size for your avatar
                .clip(CircleShape) // Clip the image in a circle shape
        ) {
            Timber.tag("JLChatSDK Image").d(url)
            val painter = rememberImagePainter(
                data = url,
                builder = {
                    // Optional: You can apply transformations to the image if needed
                    // For example, to crop the image to a circle shape
                    transformations(CircleCropTransformation())
                }
            )

            LaunchedEffect(painter) {
                // Wait for the image to load
                isLoading = when (painter.state) {
                    is ImagePainter.State.Success -> false
                    is ImagePainter.State.Error -> false
                    else -> {
                        true
                    }
                }
            }

            if (isLoading) {
                // Placeholder image while the actual image is loading
                Image(
                    painter = painterResource(R.drawable.ic_person_circle),
                    contentDescription = "Placeholder",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    painter = painter,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop, // Crop the image to fit the circle
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

fun Activity.createChatRoomWith(parameter: ChatParameter) =
    Intent(this, ChatRoomActivity::class.java).apply {
        this.putExtra(ChatRoomActivity.PARAM_DATA, parameter)
    }