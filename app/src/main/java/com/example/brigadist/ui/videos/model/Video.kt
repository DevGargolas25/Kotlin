package com.example.brigadist.ui.videos.model

data class Video(
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val tags: List<String> = emptyList(),
    val duration: String = "",
    val views: Int = 0,
    val publishedAt: String = "",
    val thumbnail: String = "",
    val description: String = "",
    val like: Int = 0,
    val url: String = ""
)