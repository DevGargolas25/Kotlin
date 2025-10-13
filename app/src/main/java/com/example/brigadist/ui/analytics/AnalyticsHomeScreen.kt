package com.example.brigadist.ui.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.brigadist.R
import com.example.brigadist.data.analytics.*
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AnalyticsHomeScreen(
    onLogout: () -> Unit,
    viewModel: AnalyticsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var isLoggingOut by remember { mutableStateOf(false) }
    var logoutError by remember { mutableStateOf<String?>(null) }
    val dashboardData by viewModel.dashboardData.collectAsState()
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = stringResource(R.string.analytics_icon_content_description),
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = stringResource(R.string.analytics_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = stringResource(R.string.analytics_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // Dashboard Content
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Permission Changes Card
            item {
                AnalyticsCard(
                    title = "Permission Changes (Last 14d)",
                    state = dashboardData.permissionChanges,
                    onRetry = { viewModel.retryPermissionChanges() }
                ) { data ->
                    val summary = data as PermissionChangesSummary
                    PermissionChangesContent(summary)
                }
            }
            
            // Permission Status Card
            item {
                AnalyticsCard(
                    title = "Permission Status (Last 14d)",
                    state = dashboardData.permissionStatus,
                    onRetry = { viewModel.retryPermissionStatus() }
                ) { data ->
                    val summary = data as PermissionStatusSummary
                    PermissionStatusContent(summary)
                }
            }
            
            // Profile Updates Card
            item {
                AnalyticsCard(
                    title = "Profile Updates (Last 14d)",
                    state = dashboardData.profileUpdates,
                    onRetry = { viewModel.retryProfileUpdates() }
                ) { data ->
                    val summary = data as ProfileUpdatesSummary
                    ProfileUpdatesContent(summary)
                }
            }
            
            // Screen Views Card
            item {
                AnalyticsCard(
                    title = "Screen Views (Last 14d)",
                    state = dashboardData.screenViews,
                    onRetry = { viewModel.retryScreenViews() }
                ) { data ->
                    val summary = data as ScreenViewsSummary
                    ScreenViewsContent(summary)
                }
            }
        }
        
        // Logout Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Button(
                onClick = {
                    isLoggingOut = true
                    logoutError = null
                    try {
                        onLogout()
                    } catch (e: Exception) {
                        logoutError = "Logout failed. Please try again."
                        isLoggingOut = false
                    }
                },
                enabled = !isLoggingOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(16.dp)
            ) {
                if (isLoggingOut) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = stringResource(R.string.logout_button),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            
            // Error message
            logoutError?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun AnalyticsCard(
    title: String,
    state: AnalyticsUiState,
    onRetry: () -> Unit,
    content: @Composable (Any) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (state is AnalyticsUiState.Error) {
                    IconButton(
                        onClick = onRetry,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retry",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Card Content
            when (state) {
                is AnalyticsUiState.Loading -> {
                    LoadingContent()
                }
                is AnalyticsUiState.Success<*> -> {
                    content(state.data as Any)
                }
                is AnalyticsUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = onRetry
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column {
        repeat(3) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(16.dp)
                        .padding(end = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                }
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(16.dp)
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        TextButton(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun PermissionChangesContent(summary: PermissionChangesSummary) {
    Column {
        summary.data.forEach { (date, data) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Location granted: ${data.location.granted}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = formatRelativeTime(summary.updatedAt),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun PermissionStatusContent(summary: PermissionStatusSummary) {
    Column {
        summary.data.forEach { (date, data) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${data.granted} users",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = formatRelativeTime(summary.updatedAt),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ProfileUpdatesContent(summary: ProfileUpdatesSummary) {
    Column {
        summary.data.forEach { (date, data) ->
            Column(
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Total: ${data.total}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Users: ${data.users}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Avg: ${data.avg}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = formatRelativeTime(summary.updatedAt),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ScreenViewsContent(summary: ScreenViewsSummary) {
    Column {
        summary.data.forEach { (date, screenViews) ->
            Column(
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                screenViews.forEach { screenView ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = screenView.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${screenView.views} views",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = formatRelativeTime(summary.updatedAt),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
