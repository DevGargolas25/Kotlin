package com.example.brigadist.ui.videos

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brigadist.ui.videos.model.Video

@Composable
fun VideosRoute(
    onVideoClick: (Video) -> Unit
) {
    val videosViewModel: VideosViewModel = viewModel()
    VideosScreen(
        viewModel = videosViewModel,
        onVideoClick = onVideoClick
    )
}