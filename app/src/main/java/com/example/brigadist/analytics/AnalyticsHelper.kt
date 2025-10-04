package com.example.brigadist.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

object AnalyticsHelper {

    private val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics

    fun trackScreenView(screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    fun trackProfileUpdate() {
        firebaseAnalytics.logEvent("profile_update", null)
    }

    fun trackPermissionStatus(permission: String, isGranted: Boolean) {
        val bundle = Bundle().apply {
            putString("permission_name", permission)
            putString("permission_status", if (isGranted) "granted" else "denied")
        }
        firebaseAnalytics.logEvent("permission_status_change", bundle)
    }
}