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
import com.example.brigadist.util.VoteLimiter
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewsViewModel(application: Application) : AndroidViewModel(application) {

    private val newsRepository = NewsRepository(application.applicationContext)
    private val connectivityManager = application.getSystemService(Application.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val firebaseDb = FirebaseDatabase.getInstance().reference.child("news")

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

        // preload local initial data (fast) - repository handles initial load policy
        viewModelScope.launch(Dispatchers.IO) {
            val localNews = newsRepository.getNewsSync()
            withContext(Dispatchers.Main) {
                _news.value = localNews
                _filteredNews.value = localNews
            }
        }

        // subscribe to repository (Firebase when online, local fallback when offline)
        viewModelScope.launch {
            newsRepository.getNews().collect { list ->
                _news.value = list
                filterAndApply(list)
            }
        }

        // trigger preload (repository's preload stores initial DB snapshot if needed)
        newsRepository.preloadNews()
    }

    private fun setupConnectivityMonitoring() {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { _isOffline.value = false }
            override fun onLost(network: Network) { _isOffline.value = true }
            override fun onCapabilitiesChanged(network: Network, nc: NetworkCapabilities) {
                val hasInternet = nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                _isOffline.value = !hasInternet
            }
        }
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
        connectivityManager.registerNetworkCallback(networkRequest, callback)

        // Check initial connectivity safely
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        val hasInternet = capabilities != null &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        _isOffline.value = !hasInternet
    }

    fun onSearchTextChange(text: String) {
        _searchText.value = text
        filterNews()
    }

    fun onTagSelected(tag: String) {
        _selectedTags.update {
            if (it.contains(tag)) it - tag else it + tag
        }
        filterNews()
    }

    private fun filterAndApply(list: List<News>) {
        _filteredNews.value = list // default; then apply search+tags
        filterNews()
    }

    private fun filterNews() {
        val search = _searchText.value.lowercase()
        val tags = _selectedTags.value

        _filteredNews.value = _news.value.filter { n ->
            val matchesText = n.title.lowercase().contains(search) || n.description.lowercase().contains(search)
            val matchesTags = tags.isEmpty() || n.tags.any { it in tags }
            matchesText && matchesTags
        }
    }

    // Voting API exposed to UI

    fun hasUserVoted(newsId: String, context: Application = getApplication()): Boolean {
        return VoteLimiter.hasVoted(context.applicationContext, newsId)
    }

    fun voteNews(newsId: String, wasUseful: Boolean, context: Application = getApplication()) {
        val appCtx = context.applicationContext
        if (VoteLimiter.hasVoted(appCtx, newsId)) return

        // increment the correct counter atomically: read current -> set current + 1
        val childKey = if (wasUseful) "usefulCount" else "notUsefulCount"
        firebaseDb.child(newsId).child(childKey).get().addOnSuccessListener { snapshot ->
            val current = snapshot.getValue(Int::class.java) ?: 0
            firebaseDb.child(newsId).child(childKey).setValue(current + 1)
            // persist local vote limiter
            VoteLimiter.saveVote(appCtx, newsId)
        }.addOnFailureListener {
            // optionally: could enqueue for retry, show snackbar, etc.
        }
    }
}
