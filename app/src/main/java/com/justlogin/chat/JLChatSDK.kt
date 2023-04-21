package com.justlogin.chat

import android.app.Application
import android.content.Context
import android.util.Log
import com.justlogin.chat.data.preference.AuthManagement
import com.justlogin.chat.module.DaggerJLComponent
import timber.log.Timber
import timber.log.Timber.Forest.plant
import javax.inject.Inject


class JLChatSDK() {

    private var isDebugable: Boolean = false
    protected lateinit var application: Application

    @Inject
    protected lateinit var authManagement: AuthManagement

    private lateinit var token: String
    fun setToken(token: String) = apply {
        this.token = token
    }

    fun enableDebug(flag: Boolean) = apply {
        isDebugable = flag
    }

    fun initSDK(application: Application) {
        this.application = application
        DaggerJLComponent.builder().application(application).build()
        if (isDebugable) {
            plant(Timber.DebugTree())
        }
        if (::token.isInitialized) {
            authManagement.saveToken(token)
        } else {
            Log.e(javaClass.simpleName, "please set token before initialize this SDK")
        }
    }

    /**
     * Call this function when got throw 401
     */
    fun refreshSDKWithNewToken(token: String) {
        apply {
            this.token = token
        }
        if (this::application.isInitialized) {
            initSDK(application)
        } else {
            Log.e(javaClass.simpleName, "SDK was not attached to any application")
        }
    }
}