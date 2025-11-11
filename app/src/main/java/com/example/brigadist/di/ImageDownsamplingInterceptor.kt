package com.example.brigadist.di

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import coil.intercept.Interceptor
import coil.request.ErrorResult
import coil.request.ImageResult
import coil.request.SuccessResult
import kotlin.math.roundToInt

/**
 * Custom interceptor that manually downsamples images to reduce memory usage.
 * This implementation uses only Android's native Bitmap APIs - no external libraries.
 * 
 * How it works:
 * 1. Intercepts successful image loads
 * 2. Extracts the bitmap from the result
 * 3. Calculates optimal target size maintaining aspect ratio
 * 4. Downsamples using Android's createScaledBitmap (bilinear filtering)
 * 5. Recycles the original bitmap to free memory
 * 6. Returns the downsampled version to be cached
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
                    // Convert other drawable types to bitmap
                    val bitmap = Bitmap.createBitmap(
                        drawable.intrinsicWidth.coerceAtLeast(1),
                        drawable.intrinsicHeight.coerceAtLeast(1),
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(bitmap)
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)
                    bitmap
                }
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
            val downsampledBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                targetSize.width,
                targetSize.height,
                true  // filter=true uses bilinear filtering for better quality
            )
            
            Log.d(TAG, "✂️ Downsampled: ${originalBitmap.width}x${originalBitmap.height} → ${downsampledBitmap.width}x${downsampledBitmap.height}")
            
            // Calculate memory savings
            val originalBytes = originalBitmap.width * originalBitmap.height * 4
            val downsampledBytes = downsampledBitmap.width * downsampledBitmap.height * 4
            val savedPercent = ((originalBytes - downsampledBytes).toFloat() / originalBytes * 100).roundToInt()
            Log.d(TAG, "💾 Memory saved: ${savedPercent}% (${(originalBytes - downsampledBytes) / 1024}KB)")
            
            // Recycle original bitmap to free memory immediately
            if (downsampledBitmap !== originalBitmap && result.drawable is BitmapDrawable) {
                originalBitmap.recycle()
            }
            
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

