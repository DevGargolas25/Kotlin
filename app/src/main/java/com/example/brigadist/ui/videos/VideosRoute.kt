package com.example.brigadist.ui.videos

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.brigadist.Orquestador
import com.example.brigadist.ui.videos.model.VideoUi

@Composable
fun VideosRoute(
    orquestador: Orquestador,
    onVideoClick: (VideoUi) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val categories = orquestador.getVideoCategories()
    var selectedCategory by rememberSaveable { mutableStateOf(categories.first()) }

    val allVideos: List<VideoUi> = orquestador.getVideos()

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

        videos = filtered,
        onVideoClick = onVideoClick // <-- pass through
    )
}
