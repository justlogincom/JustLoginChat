package com.justlogin.chat.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.justlogin.chat.domain.*
import com.justlogin.chat.module.ViewModelFactory
import javax.inject.Inject

class ChatRoomActivity : ComponentActivity() {

    @Inject
    lateinit var vmFactory: ViewModelFactory

    private val viewModel: ChatRoomViewmodel by viewModels { vmFactory }


    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var messageList = mutableListOf<String>()
            var messageCounter = 0

            // Simulate initial messages
            for (i in 1..10) {
                messageList.add("Message $i")
            }
            Scaffold(
                topBar = {
                    TopAppBar(title = { Text("Messages") })
                },
                content = {
                    LazyColumn(
                        reverseLayout = true,
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp)
                    ) {
                        itemsIndexed(messageList) { index, message ->
                            MessageItem(message = message)
                            if (index == 0) {
                                LoadMoreButton(onClick = {
                                    loadMoreMessages(messageList, messageCounter)
                                })
                            }
                        }
                    }
                }
            )
        }
    }


    @Composable
    fun MessageItem(message: String) {
        Text(text = message)
    }


    @Composable
    fun LoadMoreButton(onClick: () -> Unit) {
        Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Load More")
        }
    }

    fun loadMoreMessages(messageList: MutableList<String>, messageCounter: Int) {
        if (messageCounter < 20) {
            for (i in 1..10) {
                messageList.add("Message ${messageCounter + i}")
            }
        }
    }
}