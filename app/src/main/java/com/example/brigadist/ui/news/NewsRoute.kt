package com.example.brigadist.ui.news

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brigadist.ui.news.model.News

@Composable
fun NewsRoute(
    onNewsClick: (News) -> Unit = {}
) {
    val newsViewModel: NewsViewModel = viewModel()
    NewsScreen(
        newsViewModel = newsViewModel,
        onNewsClick = onNewsClick
    )
}

