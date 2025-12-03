package com.example.brigadist.ui.components

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageScope
import coil.compose.AsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest

/**
 * AsyncImage composable that uses Coil for image loading and caching
 * Coil automatically handles memory and disk caching
 */
@Composable
fun CachedAsyncImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    placeholder: @Composable () -> Unit = {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
) {
    if (imageUrl.isNullOrBlank()) {
        Box(modifier = modifier) {
            placeholder()
        }
        return
    }
    
    val context = LocalContext.current
    // Cache connectivity manager to avoid repeated service lookups
    val connectivityManager = remember { context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    val isOnline = remember {
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }
        capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    // Configure image request based on online/offline status
    // When online: Always fetch from network (Coil will use network if cache is empty)
    // When offline: Use cache only
    val imageRequest = if (isOnline) {
        // Online: Enable all caches but Coil will fetch from network if cache is empty
        // This ensures fresh images from Firebase when online, even if local storage was cleared
        ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED) // Cache for offline, but network takes priority when online
            .networkCachePolicy(CachePolicy.ENABLED)
            .allowHardware(false)
            .build()
    } else {
        // Offline: Use cache only
        ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.READ_ONLY) // Only read from cache when offline
            .networkCachePolicy(CachePolicy.DISABLED) // No network when offline
            .allowHardware(false)
            .build()
    }
    
    SubcomposeAsyncImage(
        model = imageRequest,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        loading = { 
            placeholder()
        },
        error = { 
            placeholder()
        },
        success = { state ->
            // Display the image using the painter from the state
            Image(
                painter = state.painter,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        }
    )
}
