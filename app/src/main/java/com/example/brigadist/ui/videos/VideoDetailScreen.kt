package com.example.brigadist.ui.videos

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brigadist.ui.videos.components.DetailVideo
import com.example.brigadist.ui.videos.model.Video

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoDetailScreen(
    video: Video,
    onBack: () -> Unit,
    videosViewModel: VideosViewModel = viewModel()
) {
    // Observe the list of videos from the view model to get live updates
    val videos by videosViewModel.videos.collectAsState()
    // Find the most up-to-date version of the video, falling back to the initial one
    val currentVideo = videos.find { it.id == video.id } ?: video

    // Increment the view count once when the screen is first launched
    LaunchedEffect(video.id) {
        videosViewModel.incrementViewCount(video.id)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        currentVideo.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        DetailVideo(
            // Pass the live, updated video object to the detail view
            video = currentVideo, 
            modifier = Modifier.padding(innerPadding),
            // Use the video ID to toggle the like status
            onLikeClicked = { videosViewModel.toggleLike(currentVideo.id, "USER_ID_PLACEHOLDER") }
        )
    }
}