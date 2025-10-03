package com.example.brigadist.ui.home.model

import com.example.brigadist.ui.videos.model.Video

data class HomeUiState(
    val notifications: List<String> = emptyList(),
    val currentNotificationIndex: Int = 0,
    val videos: List<Video> = emptyList(),
    val showMenu: Boolean = false,
    val showNotifications: Boolean = false
)
