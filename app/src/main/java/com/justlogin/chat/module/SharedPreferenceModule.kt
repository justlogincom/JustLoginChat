package com.justlogin.chat.module

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.justlogin.chat.data.preference.AuthManagement
import com.justlogin.chat.data.preference.AuthManagementImpl
import com.justlogin.chat.module.annnotate.AppScope
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SharedPreferencesModule {

    @AppScope
    @Provides
    fun provideSharedPreferences(application: Application): SharedPreferences {
        return application.getSharedPreferences("JLChat", Context.MODE_PRIVATE)
    }

    @AppScope
    @Provides
    fun provideAuthManagement(sharedPreferences: SharedPreferences): AuthManagement {
        return AuthManagementImpl(sharedPreferences)
    }
}