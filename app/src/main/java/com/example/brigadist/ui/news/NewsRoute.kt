package com.example.brigadist.ui.news

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun NewsRoute(
    onNewsClick: (com.example.brigadist.ui.news.model.News) -> Unit = {}
) {
    val newsViewModel: NewsViewModel = viewModel()
    NewsScreen(newsViewModel = newsViewModel, onNewsClick = onNewsClick)
}
