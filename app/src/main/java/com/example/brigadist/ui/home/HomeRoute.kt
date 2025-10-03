package com.example.brigadist.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brigadist.ui.home.model.HomeUiState
import com.example.brigadist.ui.videos.VideosViewModel
import com.example.brigadist.ui.videos.model.Video

@Composable
fun HomeRoute(
    onOpenVideo: (Video) -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onNavigateToVideos: () -> Unit = {},
    videosViewModel: VideosViewModel = viewModel()
) {
    val videos by videosViewModel.videos.collectAsState()
    val homeUiState = HomeUiState(
        videos = videos
    )

    HomeScreen(
        state = homeUiState,
        onOpenProfileSettings = onOpenProfile,
        onVideoClick = onOpenVideo,
        onNavigateToVideos = onNavigateToVideos
    )
}
