package com.justlogin.chat.module

import com.justlogin.chat.data.repository.ChatRepository
import com.justlogin.chat.domain.*
import dagger.Module
import dagger.Provides

@Module
class DomainModule {

    @Provides
    fun provideCreateRoom(repository: ChatRepository): CreateRoomChat = CreateRoomChat(repository)

    @Provides
    fun provideGetMessages(repository: ChatRepository): GetMessages = GetMessages(repository)

    @Provides
    fun provideSendMessage(repository: ChatRepository): SendMessage = SendMessage(repository)

    @Provides
    fun provideUpdateReadStatus(repository: ChatRepository): UpdateReadStatus = UpdateReadStatus(repository)

    @Provides
    fun provideDeleteMessage(repository: ChatRepository): DeleteMessage = DeleteMessage(repository)

    @Provides
    fun provideUpdateMessage(repository: ChatRepository): UpdateMessage = UpdateMessage(repository)

}