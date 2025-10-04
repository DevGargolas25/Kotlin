package com.example.brigadist.ui.videos

import androidx.lifecycle.ViewModel
import com.example.brigadist.data.FirebaseVideoAdapter
import com.example.brigadist.ui.videos.model.Video
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class VideosViewModel : ViewModel() {

    private val firebaseVideoAdapter = FirebaseVideoAdapter()

    private val _videos = MutableStateFlow<List<Video>>(emptyList())
    val videos = _videos.asStateFlow()

    private val _filteredVideos = MutableStateFlow<List<Video>>(emptyList())
    val filteredVideos = _filteredVideos.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
    val selectedTags = _selectedTags.asStateFlow()

    init {
        // Ahora usamos callback en lugar de Flow
        firebaseVideoAdapter.getVideos { list ->
            _videos.value = list
            _filteredVideos.value = list
        }
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
}
