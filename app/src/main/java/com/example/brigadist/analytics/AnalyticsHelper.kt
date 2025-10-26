package com.example.brigadist.analytics

import android.os.Bundle
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.result.UserProfile
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

object AnalyticsHelper {

    private val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics

    // Store account and token to be able to fetch user info on demand
    private var auth0Account: Auth0? = null
    private var accessToken: String? = null

    // Call this function after the user logs in
    fun identifyUser(account: Auth0, token: String) {
        this.auth0Account = account
        this.accessToken = token

        // Set user property for easy filtering in Firebase Console
        executeWithEmail { email ->
            firebaseAnalytics.setUserProperty("email", email)
        }
    }

    // Call this when the user logs out
    fun clearUserIdentity() {
        auth0Account = null
        accessToken = null
        firebaseAnalytics.setUserProperty("email", null)
    }

    /**
     * Helper function to fetch the user's email before executing an action.
     * This solves the race condition of events being fired before user info is available.
     */
    private fun executeWithEmail(action: (email: String?) -> Unit) {
        val account = auth0Account
        val token = accessToken

        // If we don't have credentials, execute the action immediately with no email.
        if (account == null || token == null) {
            action(null)
            return
        }

        // Fetch fresh user info from Auth0
        val client = AuthenticationAPIClient(account)
        client.userInfo(token).start(
            object : com.auth0.android.callback.Callback<UserProfile, AuthenticationException> {
                override fun onSuccess(result: UserProfile) {
                    // On success, execute the action with the fetched email.
                    action(result.email)
                }

                override fun onFailure(error: AuthenticationException) {
                    // On failure, execute the action without an email.
                    action(null)
                }
            }
        )
    }

    fun trackScreenView(screenName: String) {
        executeWithEmail { email ->
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
                email?.let { putString("user_email", it) }
            }
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
        }
    }

    fun trackProfileUpdate() {
        executeWithEmail { email ->
            val bundle = Bundle().apply {
                email?.let { putString("user_email", it) }
            }
            firebaseAnalytics.logEvent("profile_update", bundle)
        }
    }

    fun trackPermissionStatus(permission: String, isGranted: Boolean) {
        executeWithEmail { email ->
            val bundle = Bundle().apply {
                putString("permission_name", permission)
                putString("permission_status", if (isGranted) "granted" else "denied")
                email?.let { putString("user_email", it) }
            }
            firebaseAnalytics.logEvent("permission_status_change", bundle)
        }
    }
}