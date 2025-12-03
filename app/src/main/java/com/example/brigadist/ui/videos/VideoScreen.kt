package com.example.brigadist.ui.videos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
//import androidx.lifecycle.viewmodel.compose.androidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brigadist.ui.videos.components.CategoryChipsRow
import com.example.brigadist.ui.videos.components.VideoCardItem
import com.example.brigadist.ui.videos.components.VideoSearchBar
import com.example.brigadist.ui.videos.model.Video

@Composable
fun VideosScreen(
    videosViewModel: VideosViewModel = viewModel(),
    onVideoClick: (Video) -> Unit
) {
    val searchText by videosViewModel.searchText.collectAsState()
    val selectedTags by videosViewModel.selectedTags.collectAsState()
    val filteredVideos by videosViewModel.filteredVideos.collectAsState()
    val isOffline by videosViewModel.isOffline.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp) // keep above bottom bar
    ) {
        item {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "Training Videos",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(12.dp))
                    VideoSearchBar(
                        value = searchText,
                        onValueChange = videosViewModel::onSearchTextChange,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        // Show offline message when no internet is available
        if (isOffline) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
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
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Offline mode - connect to show more videos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
        item {
            Spacer(Modifier.height(12.dp))
            // Memoize tag extraction to avoid recomputation on every recomposition
            val allVideos = videosViewModel.videos.collectAsState().value
            val allTags = remember(allVideos) {
                allVideos.flatMap { it.tags }.distinct()
            }
            CategoryChipsRow(
                categories = allTags,
                selected = selectedTags,
                onSelected = { videosViewModel.onTagSelected(it) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))
        }

        // --- Render the list of cards
        items(filteredVideos, key = { it.id }) { video ->
            VideoCardItem(
                video = video,
                onClick = { onVideoClick(video) },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}