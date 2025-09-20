package com.example.brigadist.ui.videos

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.brigadist.ui.videos.model.VideoUi
import com.example.brigadist.ui.videos.model.mockVideos

@Composable
fun VideosRoute() {
    var query by rememberSaveable { mutableStateOf("") }
    val categories = listOf("All", "Safety", "Medical", "Training", "Emergency")
    var selectedCategory by rememberSaveable { mutableStateOf(categories.first()) }

    // keep the mock list stable across recompositions
    val allVideos: List<VideoUi> = remember { mockVideos() }

    // filter by category + query
    val filtered by remember(query, selectedCategory, allVideos) {
        derivedStateOf {
            val q = query.trim().lowercase()
            allVideos.filter { v ->
                val matchesCategory =
                    (selectedCategory == "All") || v.tags.any { it.equals(selectedCategory, ignoreCase = true) }
                val matchesQuery =
                    q.isBlank() || v.title.lowercase().contains(q) || v.tags.any { it.lowercase().contains(q) }
                matchesCategory && matchesQuery
            }
        }
    }

    VideosScreen(
        query = query,
        onQueryChange = { query = it },
        categories = categories,
        selectedCategory = selectedCategory,
        onCategorySelected = { selectedCategory = it },

        // NEW:
        videos = filtered,
        onVideoClick = { /* TODO: navigate to detail with it.id */ }
    )
}
