package com.example.brigadist.ui.videos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
        item {
            Spacer(Modifier.height(12.dp))
            val allTags = videosViewModel.videos.collectAsState().value.flatMap { it.tags }.distinct()
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