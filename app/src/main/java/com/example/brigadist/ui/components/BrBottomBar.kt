package com.example.brigadist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brigadist.R
import com.example.brigadist.ui.theme.DeepPurple
import com.example.brigadist.ui.theme.LightAqua

enum class Destination { Home, Chat, Map, Videos }

@Composable
fun BrBottomBar(
    selected: Destination,
    onSelect: (Destination) -> Unit,
    onSosClick: () -> Unit,           // NEW
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        // Home
        NavigationBarItem(
            selected = selected == Destination.Home,
            onClick = { onSelect(Destination.Home) },
            icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        )
        // Chat
        NavigationBarItem(
            selected = selected == Destination.Chat,
            onClick = { onSelect(Destination.Chat) },
            icon = { Icon(painterResource(R.drawable.ic_forum), contentDescription = "Chat") },
            label = { Text("Chat") }
        )
        // SOS (center action, not a destination)
        NavigationBarItem(
            selected = false,
            onClick = onSosClick,
            icon = {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = "SOS",
                    tint = MaterialTheme.colorScheme.error
                )
            },
            label = { Text("SOS", color = MaterialTheme.colorScheme.error) },
            alwaysShowLabel = true
        )
        // Map
        NavigationBarItem(
            selected = selected == Destination.Map,
            onClick = { onSelect(Destination.Map) },
            icon = { Icon(painterResource(R.drawable.ic_map), contentDescription = "Map") },
            label = { Text("Map") }
        )
        // Videos
        NavigationBarItem(
            selected = selected == Destination.Videos,
            onClick = { onSelect(Destination.Videos) },
            icon = { Icon(painterResource(R.drawable.ic_play_circle), contentDescription = "Videos") },
            label = { Text("Videos") }
        )
    }
}

// Legacy components - kept for reference but not used
// @Composable
// private fun BarItem(...) { ... }

// @Composable  
// fun BrSosFab(onClick: () -> Unit) { ... }
