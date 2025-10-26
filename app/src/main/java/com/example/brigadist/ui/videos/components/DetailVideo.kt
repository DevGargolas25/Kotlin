package com.example.brigadist.ui.videos.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.brigadist.ui.videos.model.Video

@Composable
fun DetailVideo(
    video: Video, 
    modifier: Modifier = Modifier,
    onLikeClicked: () -> Unit = {}
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(video.url))
            prepare()
            playWhenReady = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Video player
        DisposableEffect(
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                factory = {
                    PlayerView(it).apply {
                        player = exoPlayer
                    }
                }
            )
        ) {
            onDispose {
                exoPlayer.release()
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