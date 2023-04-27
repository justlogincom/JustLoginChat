package com.justlogin.chat.module

import com.justlogin.chat.common.Consts
import com.justlogin.chat.data.ChatAPI
import com.justlogin.chat.data.preference.AuthManagement
import com.justlogin.chat.module.annnotate.AppScope
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber


@Module
class NetworkModule {

    @AppScope
    @Provides
    fun provideOkHttpClient(networkConfig: Pair<String,Boolean>,interceptor: Interceptor): OkHttpClient {
        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
        if (networkConfig.second) {
            val logging = HttpLoggingInterceptor { message ->
                Timber.tag("JLChatSDK Network").d(message)
            }.apply {
                setLevel(HttpLoggingInterceptor.Level.BODY)
            }
            client.addInterceptor(logging)
        }
        return client
            .build()
    }

    @AppScope
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient,networkConfig: Pair<String,Boolean>): Retrofit {
        return Retrofit.Builder()
            .baseUrl(networkConfig.first)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @AppScope
    @Provides
    fun provideChatAPI(retrofit: Retrofit): ChatAPI {
        return retrofit.create(ChatAPI::class.java)
    }

    @AppScope
    @Provides
    fun provideInterceptor(authManagement: AuthManagement): Interceptor {
        return Interceptor { chain ->
            var request = chain.request()
            val token = getTokenFromSomewhere(authManagement)
            if (token != null) {
                request = request.newBuilder()
                    .header(Consts.AUTHORIZATION, "Bearer $token")
                    .build()
            }
            chain.proceed(request)
        }
    }

    private fun getTokenFromSomewhere(authManagement: AuthManagement): String? {
        return authManagement.getToken()
    }
}