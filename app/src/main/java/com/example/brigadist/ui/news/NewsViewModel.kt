package com.example.brigadist.ui.news

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadist.ui.news.data.repository.NewsRepository
import com.example.brigadist.ui.news.model.News
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewsViewModel(application: Application) : AndroidViewModel(application) {

    private val newsRepository = NewsRepository(application.applicationContext)
    private val connectivityManager = application.getSystemService(Application.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _news = MutableStateFlow<List<News>>(emptyList())
    val news = _news.asStateFlow()

    private val _filteredNews = MutableStateFlow<List<News>>(emptyList())
    val filteredNews = _filteredNews.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
    val selectedTags = _selectedTags.asStateFlow()
    
    private val _isOffline = MutableStateFlow(false)
    val isOffline = _isOffline.asStateFlow()

    init {
        setupConnectivityMonitoring()
        
        // Load initial data from local database first (fast, works offline)
        viewModelScope.launch(Dispatchers.IO) {
            val localNews = newsRepository.getNewsSync()
            withContext(Dispatchers.Main) {
                _news.value = localNews
                _filteredNews.value = localNews
            }
        }
        
        // Then subscribe to repository updates (Firebase when online, local when offline)
        viewModelScope.launch {
            newsRepository.getNews().collect {
                _news.value = it
                _filteredNews.value = it
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
        filterNews()
    }

    fun onTagSelected(tag: String) {
        _selectedTags.update {
            if (it.contains(tag)) {
                it - tag
            } else {
                it + tag
            }
        }
        filterNews()
    }

    private fun filterNews() {
        val searchText = _searchText.value.lowercase()
        val selectedTags = _selectedTags.value

        _filteredNews.value = _news.value.filter { newsItem ->
            val matchesSearchText = newsItem.title.lowercase().contains(searchText) ||
                    newsItem.description.lowercase().contains(searchText)
            val matchesTags = selectedTags.isEmpty() || newsItem.tags.any { it in selectedTags }
            matchesSearchText && matchesTags
        }
    }
}

