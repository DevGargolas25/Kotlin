package com.example.brigadist.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(LightAqua)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, color = DeepPurple, style = MaterialTheme.typography.bodySmall,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
        Row {
            IconButton(onClick = onBellClick) { Icon(Icons.Default.Notifications, null, tint = DeepPurple) }
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, null, tint = DeepPurple) }
        }
    }
}