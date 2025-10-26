package com.example.brigadist.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.example.brigadist.auth.User

object AnalyticsHelper {

    private val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics
    private var currentUser: User? = null

    fun setCurrentUser(user: User?) {
        currentUser = user
    }

    fun trackScreenView(screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    fun trackProfileUpdate() {
        val bundle = Bundle().apply {
            putString("user_email", currentUser?.email ?: "unknown_email")
        }
        firebaseAnalytics.logEvent("profile_update", bundle)
    }

    fun trackPermissionStatus(permission: String, isGranted: Boolean) {
        val bundle = Bundle().apply {
            putString("permission_name", permission)
            putString("permission_status", if (isGranted) "granted" else "denied")
            putString("user_email", currentUser?.email ?: "unknown_email")
        }
        firebaseAnalytics.logEvent("permission_status_change", bundle)
    }
}