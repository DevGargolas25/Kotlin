package com.example.brigadist.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.brigadist.R

// The complete list of destinations
enum class Destination {
    Home, Chat, Map, Videos, News, Emergency, Notifications
}

@Composable
fun BrBottomBar(
    selected: Destination,
    onSelect: (Destination) -> Unit,
    onSosClick: () -> Unit,
    useEmergencyAsDestination: Boolean = false,
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
            label = { Text(stringResource(R.string.chat_tab_label)) }
        )

        // SOS or Emergency (conditional based on useEmergencyAsDestination)
        if (useEmergencyAsDestination) {
            // Emergency as destination for Brigadists
            NavigationBarItem(
                selected = selected == Destination.Emergency,
                onClick = { onSelect(Destination.Emergency) },
                icon = {
                    Icon(
                        Icons.Filled.MedicalServices,
                        contentDescription = "Emergency",
                        tint = if (selected == Destination.Emergency) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                },
                label = { Text("Emergency") }
            )
        } else {
            // SOS action button for regular users
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
        }

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
