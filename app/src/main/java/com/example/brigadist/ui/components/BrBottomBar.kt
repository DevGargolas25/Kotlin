package com.example.brigadist.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.example.brigadist.ui.home.HomeScreen
import com.example.brigadist.ui.home.model.HomeSamples
import com.example.brigadist.ui.home.model.HomeUiState
import com.example.brigadist.ui.home.model.VideoCard

@Composable
fun HomeRoute(
    onOpenVideo: (VideoCard) -> Unit = {}
) {
    var state by remember {
        mutableStateOf(
            HomeUiState(
                notifications = HomeSamples.notifications,
                videos = HomeSamples.videos
            )
        )
    }
    var showMenu by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }

    HomeScreen(
        state = state,
        onTickNotification = {
            if (state.notifications.isNotEmpty()) {
                val next = (state.currentNotificationIndex + 1) % state.notifications.size
                state = state.copy(currentNotificationIndex = next)
            }
        },
        onShowAllNotifications = { showNotifications = true },
        onOpenProfileSettings = { showMenu = true },
        onLearnMore = { /* navigate to join page */ },
        onVideoClick = onOpenVideo
    )

    if (showMenu) {
        AlertDialog(onDismissRequest = { showMenu = false }, confirmButton = {},
            title = { Text("Profile & Settings") }, text = { Text("Manage your account and preferences") })
    }
    if (showNotifications) {
        AlertDialog(onDismissRequest = { showNotifications = false }, confirmButton = {},
            title = { Text("All Notifications") }, text = {
                Column {
                    state.notifications.forEach { Text("• $it") }
                }
            })
    }
}
