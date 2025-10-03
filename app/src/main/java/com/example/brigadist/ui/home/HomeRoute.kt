package com.example.brigadist.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brigadist.R
import com.example.brigadist.ui.home.model.HomeUiState
import com.example.brigadist.ui.videos.VideosViewModel
import com.example.brigadist.ui.videos.model.Video

@Composable
fun HomeRoute(
    onOpenVideo: (Video) -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onNavigateToVideos: () -> Unit = {},
    onVideoClickFromCarousel: (Video) -> Unit = {},
    videosViewModel: VideosViewModel = viewModel()
) {
    val videos by videosViewModel.videos.collectAsState()
    val homeUiState = HomeUiState(
        videos = videos
    )
    
    val context = LocalContext.current
    
    fun openInstagram() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.instagram_url)))
            context.startActivity(intent)
        } catch (e: Exception) {
            // Handle case where no app can handle the intent
            Toast.makeText(context, "Unable to open Instagram", Toast.LENGTH_SHORT).show()
        }
    }

    HomeScreen(
        state = homeUiState,
        onOpenProfileSettings = onOpenProfile,
        onVideoClick = onVideoClickFromCarousel,
        onNavigateToVideos = onNavigateToVideos,
        onLearnMore = ::openInstagram
    )
}
