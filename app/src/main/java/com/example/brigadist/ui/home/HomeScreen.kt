package com.example.brigadist.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.brigadist.ui.home.components.HomeLearnOnYourOwnSection
import com.example.brigadist.ui.home.components.HomeNotificationBar
import com.example.brigadist.ui.home.components.HomeJoinBrigadeCard
import com.example.brigadist.ui.home.components.HomeNewsCard
import com.example.brigadist.ui.home.model.HomeUiState
import com.example.brigadist.ui.notifications.data.repository.NotificationRepository
import com.example.brigadist.ui.notifications.model.Notification
import com.example.brigadist.ui.videos.model.Video
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun HomeScreen(
    state: HomeUiState,
    userName: String = "",
    onTickNotification: () -> Unit = {},
    onShowAllNotifications: () -> Unit = {},
    onOpenProfileSettings: () -> Unit,
    onLearnMore: () -> Unit = {},
    onVideoClick: (Video) -> Unit,
    onNavigateToVideos: () -> Unit,
    onNavigateToNews: () -> Unit = {},
    showOfflineAlert: Boolean = false,
    onDismissOfflineAlert: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val notificationRepo = remember { NotificationRepository(context) }

    // Sample realistic notifications in English
    val realisticNotifications = remember {
        listOf(
            "Earthquake Alert" to "A 5.8 magnitude earthquake has been detected. Stay calm and find a safe place.",
            "High Temperatures" to "Heatwave: Temperatures are expected to exceed 35°C (95°F). Stay hydrated and avoid sun exposure.",
            "Flood Warning" to "Heavy rainfall in the area. Risk of flash floods. Seek higher ground.",
            "Strong Winds" to "Wind gusts of up to 80 km/h (50 mph) are forecasted. Secure loose objects outdoors.",
            "Extreme UV Index" to "The UV index is extreme. Use sunscreen, a hat, and sunglasses if you need to go outside."
        )
    }

    LaunchedEffect(state.notifications) {
        while (true) { delay(10_000); onTickNotification() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .verticalScroll(rememberScrollState())
        ) {
            val banner = state.notifications.getOrNull(state.currentNotificationIndex).orEmpty()

            HomeNotificationBar(
                text = banner,
                onBellClick = onShowAllNotifications,
                onMenuClick = onOpenProfileSettings
            )

            Spacer(Modifier.height(4.dp))
            val greeting = if (userName.isNotBlank()) "Hi $userName!" else "Hi!"
            Text(
                greeting,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(8.dp))
            HomeJoinBrigadeCard(onJoinClick = onLearnMore)
            Spacer(Modifier.height(8.dp))
            HomeLearnOnYourOwnSection(
                videos = state.videos,
                onVideoClick = onVideoClick,
                onViewAllClick = onNavigateToVideos
            )
            Spacer(Modifier.height(8.dp))
            HomeNewsCard(onVisitNewsFeed = onNavigateToNews)

            // Test button to create a realistic notification
            Button(
                onClick = {
                    scope.launch {
                        val (title, message) = realisticNotifications.random()
                        val newNotification = Notification(
                            id = UUID.randomUUID().toString(),
                            title = title,
                            message = message,
                            timestamp = System.currentTimeMillis()
                        )
                        notificationRepo.addNotification(newNotification)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Create Test Notification")
            }
        }

        // Non-intrusive offline alert at the top
        AnimatedVisibility(
            visible = showOfflineAlert,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 60.dp, start = 16.dp, end = 16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = "No Internet",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Please connect to the internet to visit the official Brigade webpage",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismissOfflineAlert,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}
