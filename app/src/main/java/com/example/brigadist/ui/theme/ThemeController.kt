package com.example.brigadist.ui.theme

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.brigadist.core.sensors.AmbientLightMonitor
import com.example.brigadist.data.prefs.ThemePreferences

enum class ThemeMode {
    AUTO, LIGHT, DARK
}

data class ThemeState(
    val mode: ThemeMode = ThemeMode.AUTO,
    val isDark: Boolean = false
)

class ThemeController(
    private val context: Context
) : ViewModel() {
    
    private val ambientLightMonitor = AmbientLightMonitor(context)
    private val themePreferences = ThemePreferences(context)
    
    private val _themeState = MutableStateFlow(ThemeState())
    val themeState: StateFlow<ThemeState> = _themeState.asStateFlow()
    
    // Thresholds for auto theme switching
    private val lightThreshold = 1000f // lux
    private val darkThreshold = 50f // lux
    private val sustainedTimeMs = 2000L // 2 seconds
    
    private var lastSwitchTime = 0L
    private var currentLuxLevel = 0f
    
    init {
        loadThemePreference()
        setupAmbientLightListener()
    }
    
    private fun loadThemePreference() {
        val savedMode = themePreferences.getThemeMode()
        val isDark = when (savedMode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.AUTO -> false // Default to light, will be updated by sensor
        }
        
        _themeState.value = ThemeState(mode = savedMode, isDark = isDark)
        
        if (savedMode == ThemeMode.AUTO) {
            startAmbientLightMonitoring()
        }
    }
    
    fun setThemeMode(mode: ThemeMode) {
        val isDark = when (mode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.AUTO -> _themeState.value.isDark // Keep current state
        }
        
        _themeState.value = ThemeState(mode = mode, isDark = isDark)
        themePreferences.saveThemeMode(mode)
        
        // Track theme mode change
        ThemeTelemetry.trackThemeModeChanged(mode)
        
        if (mode == ThemeMode.AUTO) {
            startAmbientLightMonitoring()
        } else {
            stopAmbientLightMonitoring()
        }
    }
    
    private fun startAmbientLightMonitoring() {
        ambientLightMonitor.startMonitoring { lux ->
            handleAmbientLightChange(lux)
        }
    }
    
    private fun stopAmbientLightMonitoring() {
        ambientLightMonitor.stopMonitoring()
    }
    
    private fun handleAmbientLightChange(lux: Float) {
        currentLuxLevel = lux
        
        if (_themeState.value.mode != ThemeMode.AUTO) return
        
        val currentTime = System.currentTimeMillis()
        val shouldBeDark = lux <= darkThreshold
        val shouldBeLight = lux >= lightThreshold
        val currentIsDark = _themeState.value.isDark
        
        // Apply hysteresis logic
        when {
            shouldBeDark && !currentIsDark -> {
                // Switch to dark theme
                if (currentTime - lastSwitchTime >= sustainedTimeMs) {
                    switchToDarkTheme()
                    lastSwitchTime = currentTime
                }
            }
            shouldBeLight && currentIsDark -> {
                // Switch to light theme
                if (currentTime - lastSwitchTime >= sustainedTimeMs) {
                    switchToLightTheme()
                    lastSwitchTime = currentTime
                }
            }
            // Between thresholds - keep current theme (hysteresis)
        }
    }
    
    private fun switchToDarkTheme() {
        val previousTheme = if (_themeState.value.isDark) "dark" else "light"
        _themeState.value = _themeState.value.copy(isDark = true)
        ThemeTelemetry.trackThemeAutoSwitch(previousTheme, "dark")
    }
    
    private fun switchToLightTheme() {
        val previousTheme = if (_themeState.value.isDark) "dark" else "light"
        _themeState.value = _themeState.value.copy(isDark = false)
        ThemeTelemetry.trackThemeAutoSwitch(previousTheme, "light")
    }
    
    private fun setupAmbientLightListener() {
        // This will be called when the app lifecycle changes
    }
    
    fun onAppResumed() {
        if (_themeState.value.mode == ThemeMode.AUTO) {
            startAmbientLightMonitoring()
        }
    }
    
    fun onAppPaused() {
        stopAmbientLightMonitoring()
    }
    
    override fun onCleared() {
        super.onCleared()
        stopAmbientLightMonitoring()
    }
}
