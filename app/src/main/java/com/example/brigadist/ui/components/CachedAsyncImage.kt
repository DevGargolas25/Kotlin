package com.example.brigadist.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.example.brigadist.cache.ImageCacheManager

/**
 * Custom AsyncImage composable that uses ImageCacheManager
 * Replaces Coil's AsyncImage
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
    val context = LocalContext.current
    val cacheManager = remember { ImageCacheManager.getInstance(context) }
    
    var bitmap by remember(imageUrl) { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember(imageUrl) { mutableStateOf(true) }
    
    LaunchedEffect(imageUrl) {
        if (imageUrl == null) {
            bitmap = null
            isLoading = false
            return@LaunchedEffect
        }
        
        isLoading = true
        bitmap = cacheManager.loadImage(imageUrl)
        isLoading = false
    }
    
    when {
        isLoading -> placeholder()
        bitmap != null -> {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }
        else -> {
            // Error state - show placeholder
            placeholder()
        }
    }
}

