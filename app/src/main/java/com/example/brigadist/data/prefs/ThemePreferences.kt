package com.example.brigadist.data.prefs

import android.content.Context
import android.content.SharedPreferences
import com.example.brigadist.ui.theme.ThemeMode

class ThemePreferences(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "theme_preferences",
        Context.MODE_PRIVATE
    )
    
    private val keyThemeMode = "theme_mode"
    
    fun saveThemeMode(mode: ThemeMode) {
        prefs.edit()
            .putString(keyThemeMode, mode.name)
            .apply()
    }
    
    fun getThemeMode(): ThemeMode {
        val modeString = prefs.getString(keyThemeMode, ThemeMode.AUTO.name)
        return try {
            ThemeMode.valueOf(modeString ?: ThemeMode.AUTO.name)
        } catch (e: IllegalArgumentException) {
            ThemeMode.AUTO
        }
    }
}
