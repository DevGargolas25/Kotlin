package com.example.brigadist.ui.videos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.brigadist.ui.theme.LightAqua
import com.example.brigadist.ui.videos.components.CategoryChipsRow
import com.example.brigadist.ui.videos.components.VideoSearchBar
import com.example.brigadist.ui.videos.components.VideoCardItem
import com.example.brigadist.ui.videos.model.VideoUi

@Composable
fun VideosScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,

    // NEW:
    videos: List<VideoUi>,
    onVideoClick: (VideoUi) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp) // keep above bottom bar
    ) {
        item {
            Surface(color = LightAqua, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "Training Videos",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(Modifier.height(12.dp))
                    VideoSearchBar(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        item {
            Spacer(Modifier.height(12.dp))
            CategoryChipsRow(
                categories = categories,
                selected = selectedCategory,
                onSelected = onCategorySelected,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))
        }

        // --- Render the list of cards
        items(videos, key = { it.id }) { video ->
            VideoCardItem(
                video = video,
                onClick = { onVideoClick(video) },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}
