package com.example.brigadist.ui.videos

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadist.data.FirebaseVideoAdapter
import com.example.brigadist.data.VideoPreloader
import com.example.brigadist.ui.videos.model.Video
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VideosViewModel(application: Application) : AndroidViewModel(application) {

    private val firebaseVideoAdapter = FirebaseVideoAdapter()
    private val connectivityManager = application.getSystemService(Application.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    // Initialize VideoPreloader with application context
    // This will manage ExoPlayer instances for preloading 5 seconds of each video
    private val preloader = VideoPreloader(application.applicationContext)

    private val _videos = MutableStateFlow<List<Video>>(emptyList())
    val videos = _videos.asStateFlow()

    private val _filteredVideos = MutableStateFlow<List<Video>>(emptyList())
    val filteredVideos = _filteredVideos.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
    val selectedTags = _selectedTags.asStateFlow()
    
    private val _isOffline = MutableStateFlow(false)
    val isOffline = _isOffline.asStateFlow()


    init {
        setupConnectivityMonitoring()
        
        viewModelScope.launch {
            firebaseVideoAdapter.getVideos().collect {
                val sortedVideos = it.sortedByDescending { it.views }
                _videos.value = sortedVideos
                _filteredVideos.value = sortedVideos
                
                // Only preload videos if online, and only the last 2 viewed (LRU)
                // This launches background coroutines that download buffer for the cached videos
                if (!_isOffline.value) {
                    preloader.preloadVideos(sortedVideos)
                }
            }
        }
    }
    
    private fun setupConnectivityMonitoring() {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isOffline.value = false
            }
            
            override fun onLost(network: Network) {
                _isOffline.value = true
            }
            
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                _isOffline.value = !hasInternet
            }
        }
        
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, callback)
        
        // Check initial connectivity
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }
        val hasInternet = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        _isOffline.value = !hasInternet
    }

    fun onSearchTextChange(text: String) {
        _searchText.value = text
        filterVideos()
    }

    fun onTagSelected(tag: String) {
        _selectedTags.update {
            if (it.contains(tag)) {
                it - tag
            } else {
                it + tag
            }
        }
        filterVideos()
    }

    private fun filterVideos() {
        val searchText = _searchText.value.lowercase()
        val selectedTags = _selectedTags.value

        _filteredVideos.value = _videos.value.filter { video ->
            val matchesSearchText = video.title.lowercase().contains(searchText) ||
                    video.description.lowercase().contains(searchText)
            val matchesTags = selectedTags.isEmpty() || video.tags.any { it in selectedTags }
            matchesSearchText && matchesTags
        }
    }

    fun incrementViewCount(videoId: String) {
        firebaseVideoAdapter.incrementViewCount(videoId)
    }

    fun toggleLike(videoId: String, userId: String) {
        firebaseVideoAdapter.toggleLike(videoId, userId)
    }
    
    /**
     * Get the VideoPreloader instance to pass to composables.
     * This allows UI components to access preloaded ExoPlayer instances.
     */
    fun getPreloader(): VideoPreloader {
        return preloader
    }
    
    /**
     * Clean up resources when ViewModel is cleared.
     * This releases all ExoPlayer instances to prevent memory leaks.
     */
    override fun onCleared() {
        super.onCleared()
        // Prevent memory leaks by releasing all ExoPlayer instances
        preloader.releaseAll()
    }
}