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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brigadist.ui.components.CachedAsyncImage
import com.example.brigadist.ui.news.components.VoteSection
import com.example.brigadist.ui.news.model.News

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    news: News,
    onBack: () -> Unit,
    newsViewModel: NewsViewModel = viewModel()
) {
    val allNews by newsViewModel.news.collectAsState()
    val currentNews = allNews.find { it.id == news.id } ?: news

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(currentNews.title.ifBlank { "News Article" }, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
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
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState())) {
            if (currentNews.imageUrl.isNotBlank()) {
                CachedAsyncImage(
                    imageUrl = currentNews.imageUrl,
                    contentDescription = "News image for ${currentNews.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(250.dp)
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                if (currentNews.title.isNotBlank()) {
                    Text(text = currentNews.title, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 12.dp))
                }

                if (currentNews.description.isNotBlank()) {
                    Text(text = currentNews.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 16.dp))
                }

                if (currentNews.tags.isNotEmpty()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp).horizontalScroll(rememberScrollState())) {
                        currentNews.tags.filter { it.isNotBlank() }.forEachIndexed { i, tag ->
                            if (i > 0) Spacer(Modifier.width(8.dp))
                            // reuse VideoTagChip from videos module
                            com.example.brigadist.ui.videos.components.VideoTagChip(tag)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                VoteSection(
                    usefulCount = currentNews.usefulCount,
                    notUsefulCount = currentNews.notUsefulCount,
                    hasVoted = newsViewModel.hasUserVoted(currentNews.id),
                    onVote = { wasUseful ->
                        newsViewModel.voteNews(currentNews.id, wasUseful)
                    }
                )
            }
        }
    }
}
