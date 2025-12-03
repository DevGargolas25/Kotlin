package com.example.brigadist.ui.notifications.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brigadist.ui.notifications.NotificationViewModel
import com.example.brigadist.ui.notifications.model.Notification

/**
 * Place this composable where you want the bell icon (e.g., in HomeNotificationBar).
 * It uses NotificationViewModel internally and shows a popup with recent notifications.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationIcon(
    modifier: Modifier = Modifier,
    onSeeAllClick: () -> Unit
) {
    val vm: NotificationViewModel = viewModel()
    val list by vm.notifications.collectAsState()
    var showPopup by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(onClick = { showPopup = !showPopup }) {
            BadgedBox(
                badge = {
                    val unreadCount = list.count { !it.read }
                    if (unreadCount > 0) {
                        Badge { Text("$unreadCount") }
                    }
                }
            ) {
                Icon(
                    Icons.Filled.Notifications,
                    contentDescription = "Notificaciones",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        if (showPopup) {
            NotificationPopup(
                notifications = list,
                onDismiss = { showPopup = false },
                onSeeAllClick = {
                    showPopup = false
                    onSeeAllClick()
                },
                onNotificationClick = { notification ->
                    vm.markAsRead(notification.id)
                    // Optionally, you could navigate to a detail screen for the specific notification here
                    showPopup = false
                    onSeeAllClick() // For now, any click goes to the list
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 40.dp, end = 8.dp)
            )
        }
    }
}
