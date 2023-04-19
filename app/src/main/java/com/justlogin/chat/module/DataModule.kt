package com.justlogin.chat.module

import com.justlogin.chat.data.repository.ChatRepository
import com.justlogin.chat.data.repository.ChatRepositoryImpl
import dagger.Binds
import dagger.Module

@Module
abstract class DataModule {

    @Binds
    abstract fun bindChatRepository(repositoryImpl: ChatRepositoryImpl): ChatRepository

}