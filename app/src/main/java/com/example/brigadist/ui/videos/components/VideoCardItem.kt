package com.example.brigadist.ui.videos.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.brigadist.di.ImageLoaderModule
import com.example.brigadist.ui.videos.model.Video

@Composable
fun VideoCardItem(
    video: Video,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth()
    ) {
        val context = LocalContext.current
        Column {
            AsyncImage(
                model = video.thumbnail,
                contentDescription = "Video thumbnail for ${video.title}",
                imageLoader = ImageLoaderModule.provideImageLoader(context),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            )

            // Info block
            Column(Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 14.dp)) {
                // Title
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(10.dp))

                // Tags
                Row {
                    video.tags.forEachIndexed { i, tag ->
                        VideoTagChip(tag)
                        if (i != video.tags.lastIndex) Spacer(Modifier.width(8.dp))
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Meta: author • views • age
                Row {
                    Text(
                        text = video.author,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text("  •  ", color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = "${video.views} views",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text("  •  ", color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = video.publishedAt,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}