package com.example.brigadist.cache

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL
import java.security.MessageDigest
import kotlin.math.roundToInt

/**
 * Custom image cache manager with memory and disk caching
 * Replaces Coil's cache implementation
 */
class ImageCacheManager private constructor(context: Context) {
    
    // Memory cache (LRU)
    private val memoryCache: LruCache<String, Bitmap>
    
    // Disk cache directory
    private val diskCacheDir: File
    
    // Max sizes
    private val maxMemoryCacheSize: Int
    private val maxDiskCacheSize: Long = 50 * 1024 * 1024 // 50 MB
    
    init {
        // Calculate memory cache size (25% of available memory)
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        maxMemoryCacheSize = maxMemory / 4
        
        memoryCache = object : LruCache<String, Bitmap>(maxMemoryCacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
        }
        
        // Setup disk cache directory
        // Use filesDir instead of cacheDir for persistent storage
        // cacheDir can be cleared by Android when storage is low or app is closed
        // filesDir persists until app is uninstalled
        diskCacheDir = File(context.filesDir, "image_cache")
        if (!diskCacheDir.exists()) {
            diskCacheDir.mkdirs()
        }
        
        // Clean old files if cache is too large
        cleanDiskCacheIfNeeded()
    }
    
    companion object {
        @Volatile
        private var INSTANCE: ImageCacheManager? = null
        
        fun getInstance(context: Context): ImageCacheManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ImageCacheManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    /**
     * Load image from URL with caching
     * @param url Image URL
     * @param maxWidth Maximum width for downsampling (default 800)
     * @param maxHeight Maximum height for downsampling (default 600)
     * @return Bitmap or null if failed
     */
    suspend fun loadImage(url: String, maxWidth: Int = 800, maxHeight: Int = 600): Bitmap? {
        return withContext(Dispatchers.IO) {
            val cacheKey = urlToKey(url)
            
            // 1. Check memory cache
            memoryCache.get(cacheKey)?.let { return@withContext it }
            
            // 2. Check disk cache
            getFromDiskCache(cacheKey)?.let { bitmap ->
                // Put back in memory cache
                memoryCache.put(cacheKey, bitmap)
                return@withContext bitmap
            }
            
            // 3. Download and cache
            downloadAndCache(url, cacheKey, maxWidth, maxHeight)
        }
    }
    
    /**
     * Get from disk cache
     */
    private fun getFromDiskCache(key: String): Bitmap? {
        val file = File(diskCacheDir, key)
        if (!file.exists()) return null
        
        return try {
            FileInputStream(file).use { input ->
                BitmapFactory.decodeStream(input)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Download image and cache it with retry logic
     */
    private suspend fun downloadAndCache(
        url: String,
        key: String,
        maxWidth: Int,
        maxHeight: Int,
        retries: Int = 3
    ): Bitmap? {
        var lastException: Exception? = null
        
        repeat(retries) { attempt ->
            try {
                // Download
                val connection = URL(url).openConnection()
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                
                val bitmap = connection.getInputStream().use { input ->
                    BitmapFactory.decodeStream(input)
                } ?: return null
                
                // Downsample if needed
                val downsampled = downsampleBitmap(bitmap, maxWidth, maxHeight)
                
                // Cache in memory
                memoryCache.put(key, downsampled)
                
                // Cache on disk
                saveToDiskCache(key, downsampled)
                
                return downsampled
            } catch (e: Exception) {
                lastException = e
                if (attempt < retries - 1) {
                    delay(1000L * (attempt + 1)) // Exponential backoff
                }
            }
        }
        
        return null
    }
    
    /**
     * Save bitmap to disk cache
     */
    private fun saveToDiskCache(key: String, bitmap: Bitmap) {
        try {
            val file = File(diskCacheDir, key)
            FileOutputStream(file).use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
            }
        } catch (e: Exception) {
            // Ignore write errors
        }
    }
    
    /**
     * Downsample bitmap maintaining aspect ratio
     */
    private fun downsampleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }
        
        val widthScale = maxWidth.toFloat() / width
        val heightScale = maxHeight.toFloat() / height
        val scale = minOf(widthScale, heightScale, 1f)
        
        val newWidth = (width * scale).roundToInt()
        val newHeight = (height * scale).roundToInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    /**
     * Convert URL to cache key (MD5 hash)
     */
    private fun urlToKey(url: String): String {
        val md = MessageDigest.getInstance("MD5")
        val hash = md.digest(url.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Clean disk cache if it exceeds max size
     */
    private fun cleanDiskCacheIfNeeded() {
        try {
            var totalSize = 0L
            val files = diskCacheDir.listFiles() ?: return
            
            // Calculate total size
            files.forEach { totalSize += it.length() }
            
            if (totalSize > maxDiskCacheSize) {
                // Sort by last modified (oldest first)
                val sortedFiles = files.sortedBy { it.lastModified() }
                
                // Delete oldest files until under limit
                for (file in sortedFiles) {
                    if (totalSize <= maxDiskCacheSize) break
                    totalSize -= file.length()
                    file.delete()
                }
            }
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }
    
    /**
     * Clear all caches
     */
    fun clearCache() {
        memoryCache.evictAll()
        diskCacheDir.listFiles()?.forEach { it.delete() }
    }
    
    /**
     * Clear only memory cache
     */
    fun clearMemoryCache() {
        memoryCache.evictAll()
    }
    
    /**
     * Invalidate specific image from cache (for profile updates)
     */
    fun invalidateImage(url: String) {
        val key = urlToKey(url)
        memoryCache.remove(key)
        File(diskCacheDir, key).delete()
    }
}

