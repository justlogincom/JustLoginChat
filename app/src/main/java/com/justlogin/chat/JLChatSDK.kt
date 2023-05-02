package com.justlogin.chat

import android.app.Application
import android.util.Log
import com.justlogin.chat.data.preference.AuthManagement
import com.justlogin.chat.module.DaggerJLComponent
import com.justlogin.chat.module.JLComponent
import timber.log.Timber
import timber.log.Timber.Forest.plant
import javax.inject.Inject


class JLChatSDK() {

    protected var theme: Int? = null
    lateinit var component: JLComponent
    protected var isDebugable: Boolean = false
    private lateinit var SERVER_URL: String
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

    companion object {
        private lateinit var INSTANCE: JLChatSDK

        @JvmStatic
        fun getInstance(): JLChatSDK {
            if (!::INSTANCE.isInitialized) {
                INSTANCE = JLChatSDK()
            }
            return INSTANCE
        }
    }

    fun initSDK(application: Application) {
        this.application = application
        theme?.let { resource ->
            application.setTheme(resource)
        }
        if (isDebugable) {
            plant(Timber.DebugTree())
        }
        if (::SERVER_URL.isInitialized.not()) {
            Log.e(javaClass.simpleName, "Error Doesn't Have SERVER_URL")
        }

        component =
            DaggerJLComponent
                .builder()
                .application(this.application)
                .serverConfig(SERVER_URL to isDebugable)
                .build()
        component.inject(this)

        if (::token.isInitialized) {
            authManagement.saveToken(token)
        } else {
            Log.e(javaClass.simpleName, "please set token before initialize this SDK")
        }

    }

    fun setServerURL(url : String) = apply {
        SERVER_URL = url
    }

    fun setThemeColor(themeResource: Int) = apply {
        this.theme = themeResource
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