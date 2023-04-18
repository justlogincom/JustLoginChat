package com.justlogin.chat.module

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import com.justlogin.chat.JLChatSDK
import com.justlogin.chat.ui.ChatRoomActivity
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [SharedPreferencesModule::class, NetworkModule::class, ContextModule::class, ViewModelModule::class, CoroutinesModule::class])
interface JLComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): JLComponent
    }

    fun inject(app: JLChatSDK)
    fun inject(chatRoomActivity: ChatRoomActivity)
}