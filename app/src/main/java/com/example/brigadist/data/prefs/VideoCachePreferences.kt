package com.example.brigadist.data.prefs

import android.content.Context
import android.content.SharedPreferences
import com.example.brigadist.ui.videos.model.Video

/**
 * Manages local persistence of video list for offline access
 * Uses SharedPreferences to store video data without external libraries
 */
class VideoCachePreferences(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "video_cache_prefs",
        Context.MODE_PRIVATE
    )
    
    private val keyCachedVideos = "cached_videos"
    private val keyCacheTimestamp = "cache_timestamp"
    private val cacheValidityMs = 7 * 24 * 60 * 60 * 1000L // 7 days
    
    /**
     * Save video list to local storage
     * Uses a simple format: each video on a new line, fields separated by |
     * Format: id|title|author|url|thumbnail|views|publishedAt|description|like|duration|tags
     */
    fun saveVideos(videos: List<Video>) {
        if (videos.isEmpty()) return
        
        val sb = StringBuilder()
        videos.forEachIndexed { index, video ->
            if (index > 0) sb.append("\n")
            // Escape special characters in fields
            val tagsStr = video.tags.joinToString(",")
            sb.append("${escape(video.id)}|")
            sb.append("${escape(video.title)}|")
            sb.append("${escape(video.author)}|")
            sb.append("${escape(video.url)}|")
            sb.append("${escape(video.thumbnail)}|")
            sb.append("${video.views}|")
            sb.append("${escape(video.publishedAt)}|")
            sb.append("${escape(video.description)}|")
            sb.append("${video.like}|")
            sb.append("${escape(video.duration)}|")
            sb.append("${escape(tagsStr)}")
        }
        
        prefs.edit()
            .putString(keyCachedVideos, sb.toString())
            .putLong(keyCacheTimestamp, System.currentTimeMillis())
            .apply()
    }
    
    /**
     * Load cached video list from local storage
     */
    fun loadCachedVideos(): List<Video>? {
        val cachedData = prefs.getString(keyCachedVideos, null) ?: return null
        val cacheTimestamp = prefs.getLong(keyCacheTimestamp, 0)
        
        // Check if cache is still valid (within 7 days)
        val now = System.currentTimeMillis()
        if (now - cacheTimestamp > cacheValidityMs) {
            return null
        }
        
        if (cachedData.isEmpty()) return emptyList()
        
        return try {
            cachedData.split("\n").mapNotNull { line ->
                if (line.isEmpty()) return@mapNotNull null
                val parts = line.split("|")
                if (parts.size < 11) return@mapNotNull null
                
                val tags = if (parts[10].isEmpty()) {
                    emptyList()
                } else {
                    parts[10].split(",").filter { it.isNotEmpty() }
                }
                
                Video(
                    id = unescape(parts[0]),
                    title = unescape(parts[1]),
                    author = unescape(parts[2]),
                    url = unescape(parts[3]),
                    thumbnail = unescape(parts[4]),
                    views = parts[5].toIntOrNull() ?: 0,
                    publishedAt = unescape(parts[6]),
                    description = unescape(parts[7]),
                    like = parts[8].toIntOrNull() ?: 0,
                    duration = unescape(parts[9]),
                    tags = tags
                )
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Clear cached videos
     */
    fun clearCache() {
        prefs.edit()
            .remove(keyCachedVideos)
            .remove(keyCacheTimestamp)
            .apply()
    }
    
    /**
     * Escape special characters for storage
     */
    private fun escape(str: String): String {
        return str.replace("\\", "\\\\")
            .replace("|", "\\|")
            .replace("\n", "\\n")
    }
    
    /**
     * Unescape special characters from storage
     */
    private fun unescape(str: String): String {
        return str.replace("\\n", "\n")
            .replace("\\|", "|")
            .replace("\\\\", "\\")
    }
}

