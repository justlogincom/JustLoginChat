package com.justlogin.chat.module

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import com.justlogin.chat.JLChatSDK
import com.justlogin.chat.module.annnotate.AppScope
import com.justlogin.chat.ui.ChatRoomActivity
import com.justlogin.chat.ui.ChatRoomViewmodel
import dagger.BindsInstance
import dagger.Component
import javax.inject.Named
import javax.inject.Singleton

@AppScope
@Component(
    modules = [SharedPreferencesModule::class,
        NetworkModule::class,
        ContextModule::class,
        ViewModelModule::class,
        CoroutinesModule::class,
        DataModule::class,
        DomainModule::class]
)
interface JLComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder
        @BindsInstance
        fun serverUrl(@Named("server_url") serverUrl: String): Builder
        fun build(): JLComponent
    }

    fun inject(app: JLChatSDK)
    fun inject(chatRoomActivity: ChatRoomActivity)
}