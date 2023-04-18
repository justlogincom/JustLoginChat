package com.justlogin.chat.module

import androidx.lifecycle.ViewModel
import com.justlogin.chat.module.annnotate.ViewModelKey
import com.justlogin.chat.ui.ChatRoomViewmodel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import androidx.lifecycle.ViewModelProvider

@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(ChatRoomViewmodel::class)
    abstract fun bindMyViewModel(myViewModel: ChatRoomViewmodel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}