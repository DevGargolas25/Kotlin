package com.example.brigadist.cache

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import kotlinx.coroutines.runBlocking

/**
 * Custom DataSource that uses VideoCacheManager for caching
 * This replaces ExoPlayer's CacheDataSource
 */
@UnstableApi
class CustomCacheDataSource(
    private val context: Context,
    private val upstreamFactory: DataSource.Factory
) : DataSource {
    
    private val cacheManager = VideoCacheManager.getInstance(context)
    private var upstream: DataSource? = null
    private var cacheFile: File? = null
    private var cacheFileStream: RandomAccessFile? = null
    private var dataSpec: DataSpec? = null
    private var bytesRemaining: Long = 0
    private var isCached: Boolean = false
    
    override fun open(dataSpec: DataSpec): Long {
        this.dataSpec = dataSpec
        
        // Try to get from cache first
        cacheFile = runBlocking {
            cacheManager.getCachedVideoFile(dataSpec.uri.toString())
        }
        
        if (cacheFile != null && cacheFile!!.exists() && cacheFile!!.length() > 0) {
            // Use cached file
            isCached = true
            cacheFileStream = RandomAccessFile(cacheFile, "r")
            bytesRemaining = if (dataSpec.length != C.LENGTH_UNSET.toLong()) {
                dataSpec.length
            } else {
                cacheFile!!.length() - dataSpec.position
            }
            
            if (dataSpec.position > 0) {
                cacheFileStream!!.seek(dataSpec.position)
            }
            
            return bytesRemaining
        } else {
            // Fall back to upstream (network)
            isCached = false
            upstream = upstreamFactory.createDataSource()
            val bytesRead = upstream!!.open(dataSpec)
            bytesRemaining = bytesRead
            
            return bytesRead
        }
    }
    
    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (bytesRemaining == 0L) {
            return C.RESULT_END_OF_INPUT
        }
        
        val bytesToRead = minOf(length.toLong(), bytesRemaining).toInt()
        val bytesRead: Int
        
        if (isCached && cacheFileStream != null) {
            bytesRead = cacheFileStream!!.read(buffer, offset, bytesToRead)
        } else if (upstream != null) {
            bytesRead = upstream!!.read(buffer, offset, bytesToRead)
        } else {
            throw IOException("No data source available")
        }
        
        if (bytesRead > 0) {
            bytesRemaining -= bytesRead
        }
        
        return bytesRead
    }
    
    override fun close() {
        cacheFileStream?.close()
        upstream?.close()
        cacheFileStream = null
        upstream = null
    }
    
    override fun addTransferListener(transferListener: TransferListener) {
        upstream?.addTransferListener(transferListener)
    }
    
    override fun getUri() = dataSpec?.uri
    
    override fun getResponseHeaders() = upstream?.responseHeaders ?: emptyMap()
}

/**
 * Factory for the custom DataSource
 */
@UnstableApi
class CustomCacheDataSourceFactory(
    private val context: Context,
    private val upstreamFactory: DataSource.Factory
) : DataSource.Factory {
    override fun createDataSource(): DataSource {
        return CustomCacheDataSource(context, upstreamFactory)
    }
}

