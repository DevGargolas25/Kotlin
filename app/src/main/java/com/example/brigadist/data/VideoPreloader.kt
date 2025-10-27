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
    }

    private val players = mutableMapOf<String, ExoPlayer>()
    private val simpleCache = CacheUtil.getSimpleCache(context)

    // A factory that creates a data source capable of reading from and writing to the cache
    private val cacheDataSourceFactory = CacheDataSource.Factory()
        .setCache(simpleCache)
        .setUpstreamDataSourceFactory(DefaultDataSource.Factory(context))
        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

    fun preloadVideo(video: Video) {
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
        Log.d(TAG, "🚀 Starting preload for ${videos.size} videos")
        videos.forEach { preloadVideo(it) }
    }

    // THIS IS THE MODIFIED FUNCTION
    fun getPlayer(videoId: String, videoUrl: String): ExoPlayer? {
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
        }
    }

    fun releaseAll() {
        Log.d(TAG, "🧹 Releasing all ${players.size} preloaded players")
        CoroutineScope(Dispatchers.Main).launch {
            players.values.forEach { it.release() }
            players.clear()
            Log.d(TAG, "✅ All players released")
        }
    }
}
