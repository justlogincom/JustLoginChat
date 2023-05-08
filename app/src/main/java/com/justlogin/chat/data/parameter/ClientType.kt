package com.justlogin.chat.data.parameter

import com.justlogin.chat.R

sealed class ClientType {
    internal abstract val imageBackround: Int

    object Expense : ClientType() {
        override val imageBackround: Int
            get() = R.drawable.bg_more_expense
    }

    object Individual : ClientType() {
        override val imageBackround: Int
            get() = R.drawable.bg_more

    }

}