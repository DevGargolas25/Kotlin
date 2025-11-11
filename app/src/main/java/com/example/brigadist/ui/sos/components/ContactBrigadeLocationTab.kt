package com.example.brigadist.ui.sos.components

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.example.brigadist.Orquestador
import com.example.brigadist.ui.map.MapViewModel
import kotlin.math.*

@Composable
fun ContactBrigadeLocationTab(
    orquestador: Orquestador,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: MapViewModel = viewModel()
    
    // Brigade member location (near meeting point)
    val brigadeLocation = LatLng(4.603700, -74.065600)
    
    // Emergency location (device location or fallback)
    var emergencyLocation by remember { mutableStateOf<LatLng?>(null) }
    val fallbackEmergencyLocation = LatLng(4.603200, -74.065300)
    
    var hasLocationPermission by remember { mutableStateOf(false) }
    var showPermissionRationale by remember { mutableStateOf(false) }
    var showDeniedForeverMessage by remember { mutableStateOf(false) }
    
    // Calculate distance and ETA
    val distance = remember(emergencyLocation, brigadeLocation) {
        emergencyLocation?.let { emergency ->
            calculateDistance(emergency, brigadeLocation)
        }
    }
    
    val etaMinutes = remember(distance) {
        distance?.let { dist ->
            // Walking speed: ~5 km/h = 83.33 m/min
            (dist / 83.33).toInt()
        }
    }
    
    // Camera position to show both markers
    val cameraPosition = remember(emergencyLocation, brigadeLocation) {
        val targetLocation = emergencyLocation ?: fallbackEmergencyLocation
        CameraPosition.Builder()
            .target(
                LatLng(
                    (targetLocation.latitude + brigadeLocation.latitude) / 2,
                    (targetLocation.longitude + brigadeLocation.longitude) / 2
                )
            )
            .zoom(16f)
            .build()
    }
    
    // Permission handling
    val locationPermissions = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val allGranted = fineLocationGranted || coarseLocationGranted
        
        hasLocationPermission = allGranted
        viewModel.setLocationPermission(allGranted)
        
        if (allGranted) {
            getCurrentLocation(context) { location ->
                emergencyLocation = location
            }
        } else {
            // Check if user denied forever
            val fineLocationDeniedForever = !fineLocationGranted && 
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                !ActivityCompat.shouldShowRequestPermissionRationale(context as androidx.activity.ComponentActivity, android.Manifest.permission.ACCESS_FINE_LOCATION)
            
            if (fineLocationDeniedForever) {
                showDeniedForeverMessage = true
            }
        }
    }
    
    // Check initial permission state
    LaunchedEffect(Unit) {
        val fineLocationGranted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        hasLocationPermission = fineLocationGranted || coarseLocationGranted
        
        if (hasLocationPermission) {
            getCurrentLocation(context) { location ->
                emergencyLocation = location
            }
        } else {
            // Use fallback location
            emergencyLocation = fallbackEmergencyLocation
        }
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        // Information Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Brigade member",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Brigade Member Location",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Your location",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (hasLocationPermission) "Your Location" else "Emergency Location",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                if (distance != null && etaMinutes != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Distance",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${String.format("%.1f", distance)}m",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Column {
                            Text(
                                text = "ETA (Walking)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${etaMinutes}min",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                if (!hasLocationPermission) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (showDeniedForeverMessage) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "Location access denied permanently",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onError
                                )
                                TextButton(
                                    onClick = {
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = android.net.Uri.fromParts("package", context.packageName, null)
                                        }
                                        context.startActivity(intent)
                                    }
                                ) {
                                    Text("Open Settings")
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = { permissionLauncher.launch(locationPermissions) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enable Location Access")
                        }
                    }
                }
            }
        }
        
        // Map
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            GoogleMap(
                cameraPositionState = CameraPositionState(cameraPosition),
                modifier = Modifier.fillMaxSize(),
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission
                )
            ) {
                // Brigade member marker
                Marker(
                    state = MarkerState(position = brigadeLocation),
                    title = "Brigade Member",
                    snippet = "Nearest brigade member location",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                )
                
                // Emergency/User location marker
                emergencyLocation?.let { location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = if (hasLocationPermission) "Your Location" else "Emergency Location",
                        snippet = if (hasLocationPermission) "Your current location" else "Emergency meeting point",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )
                }
                
                // Polyline connecting the two points
                if (emergencyLocation != null) {
                    Polyline(
                        points = listOf(emergencyLocation!!, brigadeLocation),
                        color = MaterialTheme.colorScheme.primary,
                        width = 4f
                    )
                }
            }
            
            // Recenter button
            FloatingActionButton(
                onClick = {
                    // Recenter on both markers
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .semantics {
                        contentDescription = "Recenter map on emergency and brigade locations"
                    },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Recenter map"
                )
            }
        }
    }
}

private fun calculateDistance(point1: LatLng, point2: LatLng): Double {
    val earthRadius = 6371000.0 // Earth's radius in meters
    
    val lat1Rad = Math.toRadians(point1.latitude)
    val lat2Rad = Math.toRadians(point2.latitude)
    val deltaLatRad = Math.toRadians(point2.latitude - point1.latitude)
    val deltaLngRad = Math.toRadians(point2.longitude - point1.longitude)
    
    val a = sin(deltaLatRad / 2) * sin(deltaLatRad / 2) +
            cos(lat1Rad) * cos(lat2Rad) *
            sin(deltaLngRad / 2) * sin(deltaLngRad / 2)
    
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    
    return earthRadius * c
}

private fun getCurrentLocation(context: Context, onLocationReceived: (LatLng) -> Unit) {
    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    
    try {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                onLocationReceived(LatLng(it.latitude, it.longitude))
            }
        }
    } catch (e: SecurityException) {
        // Handle permission denied
    }
}
