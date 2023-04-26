package com.justlogin.chat.data.parameter

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class ChatParameter(
    val token: String,
    val userId : String,
    val userName : String,
    val participantsId: List<String>,
    val companyId : String,
    val roomId : String
):Parcelable
