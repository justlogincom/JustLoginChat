package com.justlogin.chat

class Tester {
    fun init(){
        JLChatSDK().setToken("").initSDK()
        JLChatSDK().refreshSDKWithNewToken("")
    }
}