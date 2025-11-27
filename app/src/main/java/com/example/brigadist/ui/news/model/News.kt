package com.example.brigadist.ui.news.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class News(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val tags: List<String> = emptyList()
)

