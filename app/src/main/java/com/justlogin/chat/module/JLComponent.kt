package com.justlogin.chat.module

import android.app.Application
import com.justlogin.chat.JLChatSDK
import com.justlogin.chat.data.parameter.AuthParameter
import com.justlogin.chat.module.annnotate.AppScope
import com.justlogin.chat.ui.ChatRoomActivity
import dagger.BindsInstance
import dagger.Component

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
        fun authenticator(application: AuthParameter): Builder
        @BindsInstance
        fun application(application: Application): Builder
        @BindsInstance
        fun serverConfig(networkConfig: Pair<String,Boolean>): Builder
        fun build(): JLComponent
    }

    fun inject(app: JLChatSDK)
    fun inject(chatRoomActivity: ChatRoomActivity)

}