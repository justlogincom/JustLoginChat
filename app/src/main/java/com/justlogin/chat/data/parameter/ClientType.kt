package com.justlogin.chat.data.parameter

import com.justlogin.chat.R

sealed class ClientType {
    internal abstract val imageBackround: Int
    internal abstract val senderChatBackground: String
    internal abstract val receiverChatBackground: String

    object Expense : ClientType() {
        override val imageBackround: Int
            get() = R.drawable.bg_more_expense
        override val senderChatBackground: String
            get() = "#EFF9EF"
        override val receiverChatBackground: String
            get() = "#F5FAF9"
    }

    object Individual : ClientType() {
        override val imageBackround: Int
            get() = R.drawable.bg_more
        override val senderChatBackground: String
            get() = "#EFEFF9"
        override val receiverChatBackground: String
            get() = "#F5F9FA"

    }

}