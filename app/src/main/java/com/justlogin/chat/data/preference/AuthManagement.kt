package com.justlogin.chat.data.preference

interface AuthManagement {
    fun getToken() : String?
    fun saveToken(token : String)
}