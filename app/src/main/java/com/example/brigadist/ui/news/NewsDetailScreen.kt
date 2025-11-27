package com.example.brigadist.ui.news

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brigadist.ui.components.CachedAsyncImage
import com.example.brigadist.ui.news.model.News
import com.example.brigadist.ui.videos.components.VideoTagChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    news: News,
    onBack: () -> Unit,
    newsViewModel: NewsViewModel = viewModel()
) {
    // Observe the list of news from the view model
    val allNews by newsViewModel.news.collectAsState()
    // Find the most up-to-date version of the news, falling back to the initial one
    val currentNews = allNews.find { it.id == news.id } ?: news

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        currentNews.title.ifBlank { "News Article" },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Image
            if (currentNews.imageUrl.isNotBlank()) {
                CachedAsyncImage(
                    imageUrl = currentNews.imageUrl,
                    contentDescription = "News image for ${currentNews.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                )
            }

            // Content
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Title
                if (currentNews.title.isNotBlank()) {
                    Text(
                        text = currentNews.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                // Description
                if (currentNews.description.isNotBlank()) {
                    Text(
                        text = currentNews.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Tags
                if (currentNews.tags.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .horizontalScroll(rememberScrollState())
                    ) {
                        currentNews.tags.filter { it.isNotBlank() }.forEachIndexed { i, tag ->
                            if (i > 0) Spacer(Modifier.width(8.dp))
                            VideoTagChip(tag)
                        }
                    }
                }
            }
        }
    }
}

