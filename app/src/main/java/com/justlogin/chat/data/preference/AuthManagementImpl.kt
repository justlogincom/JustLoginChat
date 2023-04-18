package com.justlogin.chat.data.preference

import android.content.SharedPreferences
import com.justlogin.chat.common.Consts.TOKEN

class AuthManagementImpl(private val sharedPref: SharedPreferences) : AuthManagement {
    override fun getToken(): String? = sharedPref.getString(TOKEN, null)
    override fun saveToken(token: String) {
        sharedPref.edit().putString(
            TOKEN, token
        ).apply()
    }
}