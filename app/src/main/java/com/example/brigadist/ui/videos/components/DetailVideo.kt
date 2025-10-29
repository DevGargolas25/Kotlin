package com.example.brigadist.ui.videos.components

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.example.brigadist.data.VideoPreloader
import com.example.brigadist.ui.videos.model.Video
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun DetailVideo(
    video: Video,
    preloader: VideoPreloader,
    isOffline: Boolean = false,
    modifier: Modifier = Modifier,
    onLikeClicked: () -> Unit
) {
    val context = LocalContext.current
    var showOfflineAlert by remember { mutableStateOf(false) }
    var playerState by remember { mutableStateOf(Player.STATE_IDLE) }
    var hasError by remember { mutableStateOf(false) }

    val exoPlayer = remember(video.id) {
        preloader.getPlayer(video.id, video.url)?.apply {
            playWhenReady = true
            playerState = playbackState
        }
    }

    // Immediately check if we should show alert when offline and no player
    LaunchedEffect(isOffline, exoPlayer) {
        if (isOffline) {
            if (exoPlayer == null) {
                // No player - wait a moment then show alert
                delay(2000)
                if (exoPlayer == null) {
                    showOfflineAlert = true
                }
            }
        }
    }

    // Use DisposableEffect to manage the listener lifecycle
    DisposableEffect(exoPlayer, isOffline) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                hasError = true
                playerState = Player.STATE_IDLE
                if (isOffline) {
                    showOfflineAlert = true
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                playerState = playbackState
                
                when (playbackState) {
                    Player.STATE_READY -> {
                        // Video loaded successfully - hide alert
                        showOfflineAlert = false
                        hasError = false
                    }
                    Player.STATE_BUFFERING -> {
                        // If offline and buffering, check after delay
                        if (isOffline) {
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(5000) // Wait 5 seconds
                                // If still buffering and offline, show alert
                                if (exoPlayer?.playbackState == Player.STATE_BUFFERING && isOffline) {
                                    showOfflineAlert = true
                                }
                            }
                        }
                    }
                    Player.STATE_IDLE -> {
                        // Player idle (possibly error) and offline
                        if (isOffline && hasError) {
                            showOfflineAlert = true
                        }
                    }
                }
            }
        }

        // Set initial state
        if (exoPlayer != null) {
            playerState = exoPlayer.playbackState
            hasError = exoPlayer.playerError != null
            if (isOffline && (hasError || playerState == Player.STATE_IDLE)) {
                showOfflineAlert = true
            }
        }

        exoPlayer?.addListener(listener)

        // The onDispose block is now correctly used within DisposableEffect
        onDispose {
            exoPlayer?.removeListener(listener)
        }
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Video player with offline alert overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            // Show player if available, or show placeholder
            if (exoPlayer != null && !showOfflineAlert) {
                DisposableEffect(
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = {
                            PlayerView(it).apply {
                                player = exoPlayer
                            }
                        }
                    )
                ) {
                    onDispose {
                        // The UI should only pause the player.
                        // The VideoPreloader is responsible for releasing it to prevent memory leaks.
                        exoPlayer?.playWhenReady = false
                    }
                }
            }
            
            // Show alert overlay if video is not loading due to no internet
            // Show when: offline AND (no player OR showOfflineAlert is true OR player has error)
            val shouldShowAlert = isOffline && (
                exoPlayer == null || 
                showOfflineAlert || 
                (exoPlayer?.playerError != null) ||
                (exoPlayer != null && playerState == Player.STATE_IDLE && hasError)
            )
            
            if (shouldShowAlert) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = "No internet. Please connect to load the video",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Title
            Text(
                text = video.title,
                style = MaterialTheme.typography.titleLarge,
            )

            Spacer(Modifier.height(8.dp))

            // Meta info row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "${video.views} views")

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onLikeClicked) {
                        Icon(Icons.Filled.ThumbUp, contentDescription = "Like")
                    }
                    Text(text = "${video.like}")
                }
            }

            Spacer(Modifier.height(16.dp))

            // Description header
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(Modifier.height(8.dp))

            // Description body
            Text(
                text = video.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
