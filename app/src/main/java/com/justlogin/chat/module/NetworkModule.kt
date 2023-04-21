package com.justlogin.chat.module

import com.justlogin.chat.BuildConfig
import com.justlogin.chat.BuildConfig.BASE_URL
import com.justlogin.chat.common.Consts
import com.justlogin.chat.data.ChatAPI
import com.justlogin.chat.data.preference.AuthManagement
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
class NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(interceptor: Interceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideChatAPI(retrofit: Retrofit) : ChatAPI{
        return retrofit.create(ChatAPI::class.java)
    }

    @Provides
    @Singleton
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