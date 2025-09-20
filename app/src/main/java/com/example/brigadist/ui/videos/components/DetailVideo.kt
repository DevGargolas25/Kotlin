package com.example.brigadist.ui.videos.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.brigadist.ui.videos.model.VideoUi

@Composable
fun DetailVideo(video: VideoUi, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = video.title,
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(8.dp))

        // Author and meta
        Text(
            text = "${video.author} • ${video.viewsText} • ${video.ageText}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(8.dp))

        // Tags
        if (video.tags.isNotEmpty()) {
            Text(
                text = "Tags: ${video.tags.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
        }

        // Duration
        val minutes = video.durationSec / 60
        val seconds = video.durationSec % 60
        Text(
            text = "Duration: %d:%02d".format(minutes, seconds),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        // Description
        Text(
            text = video.description,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
