package com.example.brigadist.core.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AmbientLightMonitor(
    private val context: Context
) {
    
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    
    private var isMonitoring = false
    private var sensorEventListener: SensorEventListener? = null
    
    // Smoothing buffer for lux values
    private val luxBuffer = mutableListOf<Float>()
    private val bufferSize = 5 // Keep last 5 readings
    
    fun startMonitoring(onLuxChanged: (Float) -> Unit) {
        if (lightSensor == null) {
            // Device doesn't have light sensor, use default behavior
            return
        }
        
        if (isMonitoring) {
            stopMonitoring()
        }
        
        isMonitoring = true
        
        sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_LIGHT) {
                    val lux = event.values[0]
                    val smoothedLux = smoothLuxValue(lux)
                    onLuxChanged(smoothedLux)
                }
            }
            
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                // Not needed for light sensor
            }
        }
        
        sensorManager.registerListener(
            sensorEventListener,
            lightSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }
    
    fun stopMonitoring() {
        if (isMonitoring && sensorEventListener != null) {
            sensorManager.unregisterListener(sensorEventListener)
            sensorEventListener = null
            isMonitoring = false
            luxBuffer.clear()
        }
    }
    
    private fun smoothLuxValue(lux: Float): Float {
        // Add new reading to buffer
        luxBuffer.add(lux)
        
        // Keep only the last bufferSize readings
        if (luxBuffer.size > bufferSize) {
            luxBuffer.removeAt(0)
        }
        
        // Calculate moving average
        return if (luxBuffer.isNotEmpty()) {
            luxBuffer.average().toFloat()
        } else {
            lux
        }
    }
    
    fun hasLightSensor(): Boolean {
        return lightSensor != null
    }
}
