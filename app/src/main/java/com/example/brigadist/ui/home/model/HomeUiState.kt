package com.example.brigadist.ui.home.model


data class HomeUiState(
    val notifications: List<String> = emptyList(),
    val currentNotificationIndex: Int = 0,
    val videos: List<VideoCard> = emptyList(),
    val showMenu: Boolean = false,
    val showNotifications: Boolean = false
)
