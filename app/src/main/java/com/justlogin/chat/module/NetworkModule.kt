package com.justlogin.chat.module

import com.google.gson.Gson
import com.justlogin.chat.common.Consts
import com.justlogin.chat.common.Consts.CLIENT_ID
import com.justlogin.chat.common.Consts.CLIENT_SECRET
import com.justlogin.chat.common.Consts.E_CLAIM_TOKEN
import com.justlogin.chat.common.Consts.GRANT_TYPE
import com.justlogin.chat.common.Consts.REFRESH_TOKEN
import com.justlogin.chat.data.ChatAPI
import com.justlogin.chat.data.parameter.AuthParameter
import com.justlogin.chat.data.parameter.AuthQuery
import com.justlogin.chat.data.preference.AuthManagement
import com.justlogin.chat.data.response.OAuthResponse
import com.justlogin.chat.module.annnotate.AppScope
import dagger.Module
import dagger.Provides
import okhttp3.Authenticator
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.io.IOException


@Module
class NetworkModule {

    @AppScope
    @Provides
    fun provideAuthenticator(
        authManagement: AuthManagement,
        networkConfig: Pair<String, Boolean>,
        authParameter: AuthParameter
    ): Authenticator {
        return TokenRenewAuthenticator(authManagement, networkConfig, authParameter)
    }

    @AppScope
    @Provides
    fun provideOkHttpClient(
        authenticator: Authenticator,
        networkConfig: Pair<String, Boolean>,
        interceptor: Interceptor,
    ): OkHttpClient {
        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .authenticator(authenticator)
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
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        networkConfig: Pair<String, Boolean>
    ): Retrofit {
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
            val token = getTokenFromPreference(authManagement)
            if (token != null) {
                request = request.newBuilder()
                    .header(Consts.AUTHORIZATION, "Bearer $token")
                    .build()
            }
            chain.proceed(request)
        }
    }

    private fun getTokenFromPreference(authManagement: AuthManagement): String? {
        return authManagement.getOauthToken()
    }

    private class TokenRenewAuthenticator(
        val authManagement: AuthManagement,
        val networkConfig: Pair<String, Boolean>,
        val authParameter: AuthParameter
    ) : Authenticator {
        private val httpClient = OkHttpClient()

        @Throws(IOException::class)
        override fun authenticate(route: Route?, response: Response): Request? {
            Timber.e("JLChatSDK renew response ${response.code}")
            if (response.code == 401) {
                val refreshToken = authManagement.getRefreshToken()
                refreshToken?.let {
                    Timber.e("JLChatSDK renew with token ${it}")
                    val oAuthResponse = renewToken(networkConfig.first)
                    Timber.e("JLChatSDK renew with new token ${oAuthResponse?.access_token}")

                    oAuthResponse?.let {
                        authManagement.saveOauthToken(oAuthResponse.access_token)
                        authManagement.saveRefreshToken(oAuthResponse.refresh_token)
                        val newAccessToken: String = oAuthResponse.access_token
                        return newRequestWithAccessToken(
                            response.request,
                            newAccessToken
                        )
                    }
                }
            }
            return null
        }

        private fun renewToken(baseUrl: String): OAuthResponse? {
            val url = "${baseUrl}v1/auth/connect/token"
            Timber.e("JLChatSDK post to $url")
            val request = Request.Builder()
                .url(url)
                .post(
                    FormBody.Builder()
                        .add(GRANT_TYPE, REFRESH_TOKEN)
                        .add(CLIENT_ID, authParameter.clientID)
                        .add(CLIENT_SECRET, authParameter.clientSecret)
                        .add(REFRESH_TOKEN, authManagement.getRefreshToken().toString())
                        .add(E_CLAIM_TOKEN, authManagement.getEclaimToken().toString())
                        .build()
                )
                .build()
            val response = httpClient.newCall(request).execute()
            return if (response.isSuccessful) {
                val result = Gson().fromJson(response.body?.string(), OAuthResponse::class.java)
                Timber.e("JLChat SDK renew result = ${result.access_token}")
                result
            } else {
                Timber.e("JLChat SDK fail renew = ${response.code}")
                null
            }
        }

        private fun newRequestWithAccessToken(request: Request, accessToken: String): Request {
            return request.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        }
    }

}

private fun AuthParameter.toAuthQuery(): AuthQuery {
    return AuthQuery(
        this.clientID,
        this.clientSecret,
        this.token,
        this.accessToken,
        this.refreshToken
    )
}
