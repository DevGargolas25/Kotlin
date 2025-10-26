package com.example.brigadist.di

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache

object ImageLoaderModule {
    private var imageLoader: ImageLoader? = null

    fun provideImageLoader(context: Context): ImageLoader {
        return imageLoader ?: synchronized(this) {
            imageLoader ?: buildImageLoader(context).also { imageLoader = it }
        }
    }

    private fun buildImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25) // Use 25% of the app's available memory for memory cache
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02) // Use 2% of the available disk space for disk cache
                    .build()
            }
            .build()
    }
}