package com.example.brigadist.ui.notifications

import androidx.compose.runtime.Composable

/**
 * Simple route wrapper. From your navigation graph, call this composable.
 */
@Composable
fun NotificationRoute(onBack: () -> Unit = {}) {
    NotificationScreen(onBack = onBack)
}
