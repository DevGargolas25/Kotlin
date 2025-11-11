package com.example.brigadist.di

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import coil.intercept.Interceptor
import coil.request.ImageResult
import coil.request.SuccessResult
import kotlin.math.roundToInt

/**
 * Custom interceptor that manually downsamples images to reduce memory usage.
 * This implementation uses only Android's native Bitmap APIs - no external libraries.
 * 
 * How it works:
 * 1. Intercepts successful image loads
 * 2. Extracts the bitmap from the result (BitmapDrawable only)
 * 3. Validates bitmap dimensions
 * 4. Calculates optimal target size maintaining aspect ratio
 * 5. Downsamples using Android's createScaledBitmap (bilinear filtering)
 * 6. Returns the downsampled version to be cached by Coil
 * 
 * Note: Original bitmap is NOT recycled - Coil manages bitmap lifecycle automatically
 */
class ImageDownsamplingInterceptor(
    private val maxWidth: Int = 800,  // Maximum width in pixels
    private val maxHeight: Int = 600,  // Maximum height in pixels
) : Interceptor {

    companion object {
        private const val TAG = "ImageDownsampler"
    }

    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        // Proceed with the original request
        val result = chain.proceed(chain.request)
        
        // Only process successful results
        if (result !is SuccessResult) {
            return result
        }

        try {
            // Extract bitmap from drawable
            val originalBitmap = when (val drawable = result.drawable) {
                is BitmapDrawable -> drawable.bitmap
                else -> {
                    // For non-BitmapDrawable, just return the original result
                    // to avoid creating large temporary bitmaps that can cause memory issues
                    Log.d(TAG, "⚠️ Non-BitmapDrawable detected, skipping downsampling")
                    return result
                }
            }
            
            // Skip if bitmap is invalid
            if (originalBitmap.width <= 0 || originalBitmap.height <= 0) {
                Log.d(TAG, "⚠️ Invalid bitmap dimensions, skipping")
                return result
            }
            
            // Calculate target dimensions
            val targetSize = calculateTargetSize(
                originalWidth = originalBitmap.width,
                originalHeight = originalBitmap.height,
                maxWidth = maxWidth,
                maxHeight = maxHeight
            )
            
            // Check if downsampling is needed
            if (targetSize.width >= originalBitmap.width && 
                targetSize.height >= originalBitmap.height) {
                // Image is already smaller than target
                Log.d(TAG, "Image already optimal: ${originalBitmap.width}x${originalBitmap.height}")
                return result
            }
            
            // Perform manual downsampling using Android's native API
            val downsampledBitmap = try {
                Bitmap.createScaledBitmap(
                    originalBitmap,
                    targetSize.width,
                    targetSize.height,
                    true  // filter=true uses bilinear filtering for better quality
                )
            } catch (e: OutOfMemoryError) {
                Log.e(TAG, "❌ OutOfMemoryError during downsampling, returning original", e)
                return result
            }
            
            Log.d(TAG, "✂️ Downsampled: ${originalBitmap.width}x${originalBitmap.height} → ${downsampledBitmap.width}x${downsampledBitmap.height}")
            
            // Calculate memory savings
            val originalBytes = originalBitmap.width * originalBitmap.height * 4
            val downsampledBytes = downsampledBitmap.width * downsampledBitmap.height * 4
            val savedPercent = ((originalBytes - downsampledBytes).toFloat() / originalBytes * 100).roundToInt()
            Log.d(TAG, "💾 Memory saved: ${savedPercent}% (${(originalBytes - downsampledBytes) / 1024}KB)")
            
            // NOTE: Do NOT recycle the original bitmap manually
            // Coil manages bitmap lifecycle automatically and recycling can cause crashes
            // when the same bitmap is accessed again during scrolling
            
            // Create new drawable with downsampled bitmap
            val newDrawable = BitmapDrawable(
                chain.request.context.resources,
                downsampledBitmap
            )
            
            // Return modified result
            return SuccessResult(
                drawable = newDrawable,
                request = result.request,
                dataSource = result.dataSource
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error downsampling image", e)
            return result
        }
    }

    /**
     * Calculate target dimensions while maintaining aspect ratio.
     * Never upscales - only downscales if image is larger than max dimensions.
     */
    private fun calculateTargetSize(
        originalWidth: Int,
        originalHeight: Int,
        maxWidth: Int,
        maxHeight: Int
    ): Size {
        // Calculate scaling factor to fit within max dimensions
        val widthScale = maxWidth.toFloat() / originalWidth
        val heightScale = maxHeight.toFloat() / originalHeight
        val scale = minOf(widthScale, heightScale, 1f) // Don't upscale (max scale is 1.0)
        
        return Size(
            width = (originalWidth * scale).roundToInt(),
            height = (originalHeight * scale).roundToInt()
        )
    }

    /**
     * Simple data class for dimensions
     */
    private data class Size(val width: Int, val height: Int)
}

