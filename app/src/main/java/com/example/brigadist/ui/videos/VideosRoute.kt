package com.example.brigadist.ui.videos

import androidx.compose.runtime.Composable
//import androidx.lifecycle.viewmodel.compose.androidViewModel
import com.example.brigadist.ui.videos.model.Video
import androidx.lifecycle.viewmodel.compose.viewModel
@Composable
fun VideosRoute(
    onVideoClick: (Video) -> Unit
) {
    // Use androidViewModel() to provide Application context needed for VideoPreloader
    val videosViewModel: VideosViewModel = viewModel()
    VideosScreen(
        videosViewModel = videosViewModel,
        onVideoClick = onVideoClick
    )
}