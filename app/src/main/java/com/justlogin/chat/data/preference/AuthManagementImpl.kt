package com.justlogin.chat.data.preference

import android.content.SharedPreferences
import com.justlogin.chat.common.Consts.ECLAIM_TOKEN
import com.justlogin.chat.common.Consts.OAUTH_TOKEN
import com.justlogin.chat.common.Consts.REFRESH_TOKEN

class AuthManagementImpl(private val sharedPref: SharedPreferences) : AuthManagement {
    override fun getOauthToken(): String? = sharedPref.getString(OAUTH_TOKEN, null)
    override fun saveOauthToken(token: String) {
        sharedPref.edit().putString(
            OAUTH_TOKEN, token
        ).apply()
    }

    override fun getRefreshToken(): String? = sharedPref.getString(REFRESH_TOKEN, null)

    override fun saveRefreshToken(token: String) {
        sharedPref.edit().putString(
            REFRESH_TOKEN, token
        ).apply()
    }

    override fun getEclaimToken(): String? = sharedPref.getString(ECLAIM_TOKEN, null)

    override fun saveEclaimToken(token: String) {
        sharedPref.edit().putString(
            ECLAIM_TOKEN, token
        ).apply()
    }
}