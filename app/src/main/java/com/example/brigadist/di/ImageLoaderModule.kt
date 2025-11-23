package com.example.brigadist.di

import android.content.Context
import com.example.brigadist.cache.ImageCacheManager

/**
 * ImageLoaderModule - Provides access to ImageCacheManager
 * Replaces Coil's ImageLoader
 */
object ImageLoaderModule {
    /**
     * Get ImageCacheManager instance
     * This method is kept for backward compatibility with existing code
     * but now returns ImageCacheManager instead of Coil's ImageLoader
     */
    fun provideImageLoader(context: Context): ImageCacheManager {
        return ImageCacheManager.getInstance(context)
    }
}