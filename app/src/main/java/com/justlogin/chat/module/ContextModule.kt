package com.justlogin.chat.module

import android.app.Application
import android.content.Context
import com.justlogin.chat.module.annnotate.AppScope
import dagger.Binds
import dagger.Module

@Module
abstract class ContextModule {
    @AppScope
    @Binds
    abstract fun bindContext(application: Application?): Context?
}