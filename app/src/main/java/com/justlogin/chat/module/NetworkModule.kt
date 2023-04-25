package com.justlogin.chat.module

import com.justlogin.chat.BuildConfig
import com.justlogin.chat.BuildConfig.BASE_URL
import com.justlogin.chat.common.Consts
import com.justlogin.chat.data.ChatAPI
import com.justlogin.chat.data.preference.AuthManagement
import com.justlogin.chat.module.annnotate.AppScope
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
class NetworkModule {

    @AppScope
    @Provides
    fun provideOkHttpClient(interceptor: Interceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }

    @AppScope
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @AppScope
    @Provides
    fun provideChatAPI(retrofit: Retrofit) : ChatAPI{
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