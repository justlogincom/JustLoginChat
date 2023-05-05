package com.justlogin.chat.data.preference

interface AuthManagement {
    fun getOauthToken() : String?
    fun saveOauthToken(token : String)

    fun getRefreshToken() : String?
    fun saveRefreshToken(token : String)

    fun getEclaimToken() : String?
    fun saveEclaimToken(token : String)
}