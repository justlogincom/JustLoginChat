package com.justlogin.chat.data.parameter

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class ChatParameter(
    private val token: String,
    private val userId : String,
    val userName : String,
    private val participantsId: List<String>,
    private val companyId : String,
    private val roomId : String
):Parcelable {
    fun getToken() = token
    fun getRoomId() = roomId.sanitize()
    fun getUserId() = userId.sanitize()

    fun getParticipantsIds() = participantsId.map {
        it.sanitize()
    }

    fun getCompanyId() = companyId.sanitize()
}

fun String.sanitize() = this.replace("-","").uppercase()
