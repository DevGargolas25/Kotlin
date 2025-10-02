package com.example.brigadist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.example.brigadist.auth.User
import com.example.brigadist.auth.credentialsToUser
import com.example.brigadist.ui.login.LoginScreen
import com.example.brigadist.ui.theme.BrigadistTheme

class MainActivity : ComponentActivity() {

    private lateinit var account: Auth0
    private var user by mutableStateOf<User?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        account = Auth0(
            getString(R.string.auth0_client_id),
            getString(R.string.auth0_domain)
        )

        setContent {
            BrigadistTheme {
                if (user == null) {
                    LoginScreen(onLoginClick = { login() })
                } else {
                    NavShell(user = user!!, onLogout = { logout() })
                }
            }
        }
    }

    private fun login() {
        WebAuthProvider.login(account)
            .withScheme("com.example.brigadist")
            .withScope("openid profile email")
            .start(this, object : Callback<Credentials, AuthenticationException> {
                override fun onFailure(error: AuthenticationException) {
                    // Handle error
                }

                override fun onSuccess(result: Credentials) {
                    user = credentialsToUser(result)
                }
            })
    }

    private fun logout() {
        WebAuthProvider.logout(account)
            .withScheme("com.example.brigadist")
            .start(this, object : Callback<Void?, AuthenticationException> {
                override fun onFailure(error: AuthenticationException) {
                    // Handle error
                }

                override fun onSuccess(result: Void?) {
                    user = null
                }
            })
    }
}