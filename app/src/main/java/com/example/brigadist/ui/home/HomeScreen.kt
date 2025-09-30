package com.example.brigadist.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.brigadist.ui.home.components.HomeLearnOnYourOwnSection
import com.example.brigadist.ui.home.components.HomeNotificationBar
import com.example.brigadist.ui.home.components.HomeJoinBrigadeCard
import com.example.brigadist.ui.home.model.HomeUiState
import com.example.brigadist.ui.home.model.VideoCard
import com.example.brigadist.ui.theme.DeepPurple
import com.example.brigadist.ui.theme.SoftWhite
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    state: HomeUiState,
    onTickNotification: () -> Unit,
    onShowAllNotifications: () -> Unit,
    onOpenProfileSettings: () -> Unit,
    onLearnMore: () -> Unit,
    onVideoClick: (VideoCard) -> Unit,
    onNavigateToVideos: () -> Unit
) {
    LaunchedEffect(state.notifications) {
        while (true) { delay(10_000); onTickNotification() }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant).padding(bottom = 80.dp)
    ) {
        val banner = state.notifications.getOrNull(state.currentNotificationIndex).orEmpty()

        HomeNotificationBar(
            text = banner,
            onBellClick = onShowAllNotifications,
            onMenuClick = onOpenProfileSettings
        )

        Spacer(Modifier.height(8.dp))
        Text("Hi John!", style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 16.dp))

        Spacer(Modifier.height(16.dp))
        HomeJoinBrigadeCard(onLearnMore)
        Spacer(Modifier.height(16.dp))
        HomeLearnOnYourOwnSection(
            videos = state.videos,
            onVideoClick = onVideoClick,
            onViewAllClick = onNavigateToVideos
        )

    }
}

