package com.example.brigadist.data

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.example.brigadist.ui.videos.model.Video
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext




@UnstableApi
class VideoPreloader(private val context: Context) {

    private companion object {
        private const val TAG = "VideoPreloader"
        private const val MAX_CACHED_VIDEOS = 2 // Only keep last 2 videos in cache
    }

    private val players = mutableMapOf<String, ExoPlayer>()
    private val simpleCache = CacheUtil.getSimpleCache(context)
    
    // LRU tracking: most recently viewed videos first (max 2)
    private val lruOrder = mutableListOf<String>()

    // A factory that creates a data source capable of reading from and writing to the cache
    private val cacheDataSourceFactory = CacheDataSource.Factory()
        .setCache(simpleCache)
        .setUpstreamDataSourceFactory(DefaultDataSource.Factory(context))
        // Removed FLAG_IGNORE_CACHE_ON_ERROR to allow offline playback from cache
    
    /**
     * Marks a video as recently viewed and manages LRU cache (only 2 videos max)
     */
    fun markVideoAsViewed(videoId: String) {
        synchronized(lruOrder) {
            // Remove from list if it already exists
            lruOrder.remove(videoId)
            // Add to front (most recent)
            lruOrder.add(0, videoId)
            
            // If we have more than MAX_CACHED_VIDEOS, release the oldest ones
            while (lruOrder.size > MAX_CACHED_VIDEOS) {
                val oldestVideoId = lruOrder.removeAt(lruOrder.size - 1)
                Log.d(TAG, "🗑️ Releasing oldest video from cache (LRU): $oldestVideoId")
                releasePlayer(oldestVideoId)
            }
        }
    }

    fun preloadVideo(video: Video) {
        // Only preload if this video is in our LRU cache (last 2 videos)
        synchronized(lruOrder) {
            if (lruOrder.size >= MAX_CACHED_VIDEOS && video.id !in lruOrder) {
                Log.d(TAG, "⏭️ Skipping preload - video not in LRU cache (max $MAX_CACHED_VIDEOS): ${video.title}")
                return
            }
            // If we have space and this video isn't in the LRU list, add it
            if (video.id !in lruOrder && lruOrder.size < MAX_CACHED_VIDEOS) {
                lruOrder.add(0, video.id)
            }
        }
        
        if (players.containsKey(video.id)) {
            Log.d(TAG, "⏭️ Skipping preload - player already exists for: ${video.title}")
            return
        }

        Log.d(TAG, "🔵 Starting preload for video: ${video.title} (ID: ${video.id})")

        // Use IO dispatcher for disk caching
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // The media source knows how to use the cache
                val mediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(video.url))

                // Switch to Main thread to create and prepare the player
                withContext(Dispatchers.Main) {
                    val player = ExoPlayer.Builder(context)
                        .build()
                        .apply {
                            setMediaSource(mediaSource)
                            prepare()
                            playWhenReady = false
                        }

                    players[video.id] = player
                    Log.d(TAG, "✅ Successfully preloaded video: ${video.title}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error preloading video: ${video.title}", e)
            }
        }
    }

    fun preloadVideos(videos: List<Video>) {
        // Only preload the first 2 videos initially (the most viewed ones)
        // They will be reordered as users view videos
        val videosToPreload = videos.take(MAX_CACHED_VIDEOS)
        Log.d(TAG, "🚀 Starting preload for ${videosToPreload.size} videos (LRU limit: $MAX_CACHED_VIDEOS)")
        videosToPreload.forEach { preloadVideo(it) }
        // Initialize LRU order with these videos
        synchronized(lruOrder) {
            lruOrder.clear()
            lruOrder.addAll(videosToPreload.map { it.id })
        }
    }

    // THIS IS THE MODIFIED FUNCTION
    fun getPlayer(videoId: String, videoUrl: String): ExoPlayer? {
        // Mark this video as recently viewed (updates LRU order)
        markVideoAsViewed(videoId)
        
        // First, check if a preloaded player already exists in memory.
        if (players.containsKey(videoId)) {
            Log.d(TAG, "🎯 Retrieved preloaded player from memory for video ID: $videoId")
            return players[videoId]
        }

        // If not, create a new player on-demand that uses the cache.
        // This will be very fast if the video is already cached on disk.
        Log.d(TAG, "⚠️ No player in memory for ID: $videoId. Creating a new one from cache.")

        try {
            // The media source knows how to use the cache
            val mediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                .createMediaSource(MediaItem.fromUri(videoUrl))

            // Create and prepare a new player
            val player = ExoPlayer.Builder(context)
                .build()
                .apply {
                    setMediaSource(mediaSource)
                    prepare() // This will be near-instant if cached
                    playWhenReady = false
                }

            // Store this new player in the map for future use
            players[videoId] = player
            Log.d(TAG, "✅ Created new player from cache for video: $videoId")
            return player

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to create on-demand player for video: $videoId", e)
            return null
        }
    }

    // The release functions remain the same, they correctly use Dispatchers.Main
    fun releasePlayer(videoId: String) {
        Log.d(TAG, "🗑️ Releasing player for video ID: $videoId")
        CoroutineScope(Dispatchers.Main).launch {
            players[videoId]?.release()
            players.remove(videoId)
            // Remove from LRU order as well
            synchronized(lruOrder) {
                lruOrder.remove(videoId)
            }
        }
    }

    fun releaseAll() {
        Log.d(TAG, "🧹 Releasing all ${players.size} preloaded players")
        CoroutineScope(Dispatchers.Main).launch {
            players.values.forEach { it.release() }
            players.clear()
            // Clear LRU order as well
            synchronized(lruOrder) {
                lruOrder.clear()
            }
            Log.d(TAG, "✅ All players released")
        }
    }
}
