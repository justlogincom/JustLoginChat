package com.justlogin.chat

import android.app.Application
import android.content.Context
import com.justlogin.chat.data.preference.AuthManagement
import com.justlogin.chat.module.DaggerJLComponent
import com.justlogin.chat.module.JLComponent
import javax.inject.Inject

class JLChatSDK : Application() {

    @Inject
    lateinit var authManagement: AuthManagement

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        DaggerJLComponent.builder().application(this).build()
        super.onCreate()
    }

    private lateinit var token: String
    fun setToken(token: String) = apply {
        this.token = token
    }

    fun initSDK() {
        authManagement.saveToken(token)
    }

    /**
     * Call this function when got throw 401
     */
    fun refreshSDKWithNewToken(token: String) {
        apply {
            this.token = token
        }
        initSDK()
    }
}