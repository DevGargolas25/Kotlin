package com.example.brigadist

import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadist.analytics.AnalyticsHelper
import com.example.brigadist.auth.User
import com.example.brigadist.ui.theme.ThemeController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppState(
    val isReady: Boolean = false,
    val user: User? = null
)

class AppOrchestrator(
    private val context: Context
) : ViewModel() {

    private val themeController = ThemeController(context)

    private val _appState = MutableStateFlow(AppState())
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    val themeState = themeController.themeState

    fun setUser(user: User?) {
        _appState.value = _appState.value.copy(
            user = user,
            isReady = true
        )
    }

    fun onAppResumed() {
        themeController.onAppResumed()
    }

    fun onAppPaused() {
        themeController.onAppPaused()
    }

    fun trackScreenView(screenName: String) {
        AnalyticsHelper.trackScreenView(screenName)
    }

    override fun onCleared() {
        super.onCleared()
        // ThemeController will handle its own cleanup
    }
}