# JustLoginChat
[![](https://jitpack.io/v/justlogincom/JustLoginChat.svg)](https://jitpack.io/#justlogincom/JustLoginChat) </br>
This is Chat SDK for Android App


Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.justlogincom:JustLoginChat:last_version_of_jitpack'
	}

Add initializer to the client size </br>
 ```
 JLChatSDK.getInstance()
                .setAuthenticator(
                    AuthParameter(
                        clientType = ClientType.Expense or ClientType.Individual,
                        clientID = "ServerClientID",
                        clientSecret = "ServerClientSecret",
                        token = "oAuth_Token",
                        accessToken = "access_Token",
                        refreshToken = "refresh_Token"
                    )
                )
                .setThemeColor(R.style.AppTheme)
                .enableDebug(BuildConfig.DEBUG)
                .setServerURL("server_url")
                .initSDK(applicationContext as App)
		
