package com.example.brigadist.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.brigadist.ui.theme.DeepPurple
import com.example.brigadist.ui.theme.LightAqua


@Composable
fun HomeNotificationBar(
    text: String,
    onBellClick: () -> Unit,
    onMenuClick: () -> Unit,        // keep the same callback; it's your profile/settings action
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onBellClick) {
            Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.onSurface)
        }
        IconButton(onClick = onMenuClick) {
            Icon(
                imageVector = Icons.Outlined.AccountCircle, // <-- outlined user/profile icon
                contentDescription = "Profile & Settings",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}