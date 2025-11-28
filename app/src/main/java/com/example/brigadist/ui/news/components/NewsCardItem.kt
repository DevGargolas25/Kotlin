package com.example.brigadist.ui.news.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.brigadist.ui.components.CachedAsyncImage
import com.example.brigadist.ui.news.model.News
import com.example.brigadist.ui.videos.components.VideoTagChip

@Composable
fun NewsCardItem(
    news: News,
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
        Column {
            // Image - only show if imageUrl is not empty
            if (news.imageUrl.isNotBlank()) {
                CachedAsyncImage(
                    imageUrl = news.imageUrl,
                    contentDescription = "News image for ${news.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                )
            }

            // Info block
            Column(Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 14.dp)) {
                // Title
                if (news.title.isNotBlank()) {
                    Text(
                        text = news.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Description
                if (news.description.isNotBlank()) {
                    Text(
                        text = news.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Tags - only show if there are tags
                if (news.tags.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    // Memoize filtered tags to avoid recomputation
                    val filteredTags = remember(news.tags) {
                        news.tags.filter { it.isNotBlank() }
                    }
                    // Use a horizontal scrollable row for tags
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        filteredTags.forEachIndexed { i, tag ->
                            if (i > 0) Spacer(Modifier.width(8.dp))
                            VideoTagChip(tag)
                        }
                    }
                }
            }
        }
    }
}

