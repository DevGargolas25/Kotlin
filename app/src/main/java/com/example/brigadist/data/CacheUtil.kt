package com.example.brigadist.data

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

@UnstableApi
object CacheUtil {

    private var simpleCache: SimpleCache? = null
    private const val MAX_CACHE_SIZE_BYTES: Long = 70 * 1024 * 1024 // 70 MB

    @Synchronized
    fun getSimpleCache(context: Context): SimpleCache {
        if (simpleCache == null) {
            val cacheDirectory = File(context.cacheDir, "media")
            val databaseProvider = StandaloneDatabaseProvider(context)
            simpleCache = SimpleCache(
                cacheDirectory,
                LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE_BYTES),
                databaseProvider
            )
        }
        return simpleCache!!
    }
}
