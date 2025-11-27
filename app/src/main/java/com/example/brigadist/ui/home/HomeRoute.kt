package com.example.brigadist.ui.home

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brigadist.R
import com.example.brigadist.ui.home.model.HomeUiState
import com.example.brigadist.ui.videos.VideosViewModel
import com.example.brigadist.ui.videos.model.Video

@Composable
fun HomeRoute(
    userName: String = "",
    onOpenVideo: (Video) -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onNavigateToVideos: () -> Unit = {},
    onNavigateToNews: () -> Unit = {},
    onVideoClickFromCarousel: (Video) -> Unit = {},
    videosViewModel: VideosViewModel = viewModel()
) {
    val videos by videosViewModel.videos.collectAsState()
    val homeUiState = HomeUiState(
        videos = videos
    )
    
    val context = LocalContext.current
    var showOfflineAlert by remember { mutableStateOf(false) }
    
    fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    fun openInstagram() {
        if (!isOnline()) {
            // Show offline alert
            showOfflineAlert = true
            return
        }
        
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
        userName = userName,
        onOpenProfileSettings = onOpenProfile,
        onVideoClick = onVideoClickFromCarousel,
        onNavigateToVideos = onNavigateToVideos,
        onNavigateToNews = onNavigateToNews,
        onLearnMore = ::openInstagram,
        showOfflineAlert = showOfflineAlert,
        onDismissOfflineAlert = { showOfflineAlert = false }
    )
}
