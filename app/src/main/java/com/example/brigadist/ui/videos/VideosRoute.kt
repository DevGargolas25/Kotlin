package com.example.brigadist.ui.videos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.brigadist.di.ImageLoaderModule
import com.example.brigadist.ui.videos.model.Video

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideosRoute(
    onVideoClick: (Video) -> Unit
) {
    val videosViewModel: VideosViewModel = viewModel()
    val searchText by videosViewModel.searchText.collectAsState()
    val selectedTags by videosViewModel.selectedTags.collectAsState()
    val videos by videosViewModel.filteredVideos.collectAsState()
    val allTags = videosViewModel.videos.collectAsState().value.flatMap { it.tags }.distinct()

    Column {
        TextField(
            value = searchText,
            onValueChange = videosViewModel::onSearchTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            label = { Text("Search") }
        )

        LazyRow(modifier = Modifier.padding(horizontal = 16.dp)) {
            items(allTags) { tag ->
                FilterChip(
                    selected = selectedTags.contains(tag),
                    onClick = { videosViewModel.onTagSelected(tag) },
                    label = { Text(tag) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }

        LazyColumn {
            items(videos) { video ->
                VideoListItem(video = video, onClick = { onVideoClick(video) })
            }
        }
    }
}

@Composable
fun VideoListItem(
    video: Video,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        AsyncImage(
            model = video.thumbnail,
            contentDescription = "Thumbnail for ${video.title}",
            contentScale = ContentScale.Crop,
            imageLoader = ImageLoaderModule.provideImageLoader(context),
            modifier = Modifier
                .width(120.dp)
                .height(67.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(text = video.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = video.duration, style = MaterialTheme.typography.bodySmall)
        }
    }
}
