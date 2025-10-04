package com.example.brigadist.ui.map

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.example.brigadist.Orquestador
import com.example.brigadist.analytics.AnalyticsHelper
import com.example.brigadist.data.MapLocation
import com.example.brigadist.ui.map.components.RecenterButton
import com.example.brigadist.ui.map.components.MapTypeSelector
import com.example.brigadist.ui.map.components.EvacuationPointBanner

@Composable
fun MapScreen(orquestador: Orquestador) {
    val context = LocalContext.current
    val viewModel: MapViewModel = viewModel()
    
    var cameraPosition by remember { mutableStateOf(orquestador.getDefaultCameraPosition()) }
    
    // Listen to ViewModel camera position changes
    val viewModelCameraPosition by viewModel.cameraPosition.collectAsState()
    
    // Update local camera position when ViewModel changes it
    LaunchedEffect(viewModelCameraPosition) {
        cameraPosition = viewModelCameraPosition
    }
    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var showPermissionRationale by remember { mutableStateOf(false) }
    var showDeniedForeverMessage by remember { mutableStateOf(false) }

    // Define location permissions
    val locationPermissions = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val allGranted = fineLocationGranted || coarseLocationGranted
        
        hasLocationPermission = allGranted
        viewModel.setLocationPermission(allGranted)
        AnalyticsHelper.trackPermissionStatus("location", allGranted)
        
        if (allGranted) {
            // Permission granted, request location and center camera
            viewModel.handleMyLocationButtonClick()
        } else {
            // Check if user denied forever
            val fineLocationDeniedForever = !fineLocationGranted && 
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                !ActivityCompat.shouldShowRequestPermissionRationale(context as androidx.activity.ComponentActivity, android.Manifest.permission.ACCESS_FINE_LOCATION)
            
            val coarseLocationDeniedForever = !coarseLocationGranted && 
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                !ActivityCompat.shouldShowRequestPermissionRationale(context as androidx.activity.ComponentActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION)
            
            if (fineLocationDeniedForever || coarseLocationDeniedForever) {
                showDeniedForeverMessage = true
            }
        }
    }

    // Check initial permission state
    LaunchedEffect(Unit) {
        val fineLocationGranted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        hasLocationPermission = fineLocationGranted || coarseLocationGranted
        viewModel.setLocationPermission(hasLocationPermission)
    }

    // Refresh permission state when returning from settings
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Check permission state when app resumes (e.g., returning from settings)
                val fineLocationGranted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                val coarseLocationGranted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                val newPermissionState = fineLocationGranted || coarseLocationGranted
                
                if (newPermissionState != hasLocationPermission) {
                    hasLocationPermission = newPermissionState
                    viewModel.setLocationPermission(newPermissionState)
                    AnalyticsHelper.trackPermissionStatus("location", newPermissionState)
                    
                    // If permission was just granted, request location
                    if (newPermissionState) {
                        viewModel.handleMyLocationButtonClick()
                    }
                }
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Initialize ViewModel with context
    LaunchedEffect(Unit) {
        viewModel.initializeLocationClient(context)
    }

    // Track map opened event
    LaunchedEffect(Unit) {
        MapTelemetry.trackMapOpened(hasLocationPermission, mapType)
    }

    // Request location updates when permission is granted
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            viewModel.requestLocationUpdate()
        }
    }

    // Collect ViewModel state
    val userLocation by viewModel.userLocation.collectAsState()
    val nearestLocation by viewModel.nearestLocation.collectAsState()
    val distanceMeters by viewModel.distanceMeters.collectAsState()
    val etaMinutes by viewModel.etaMinutes.collectAsState()
    val locationRequestStatus by viewModel.locationRequestStatus.collectAsState()
    
    // Snackbar state for location feedback
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show snackbar based on location request status
    LaunchedEffect(locationRequestStatus) {
        val status = locationRequestStatus
        when (status) {
            is LocationRequestStatus.Error -> {
                snackbarHostState.showSnackbar(
                    message = status.message,
                    duration = SnackbarDuration.Short
                )
            }
            is LocationRequestStatus.Success -> {
                snackbarHostState.showSnackbar(
                    message = "Location found",
                    duration = SnackbarDuration.Short
                )
            }
            else -> { /* No action for Idle/Requesting */ }
        }
    }

    // Get all evacuation points for markers
    val evacuationPoints = remember { viewModel.getAllEvacuationPoints() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = CameraPositionState(
                position = cameraPosition
            ),
            properties = MapProperties(
                mapType = mapType,
                isMyLocationEnabled = hasLocationPermission
            )
        ) {
            // Add markers for all evacuation points
            evacuationPoints.forEach { point ->
                val isNearest = nearestLocation?.name == point.name
                Marker(
                    state = MarkerState(position = point.latLng),
                    title = point.name,
                    snippet = point.description,
                    contentDescription = "Evacuation point: ${point.name}",
                    icon = if (isNearest) {
                        // Use a different icon for the nearest point
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    } else {
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    }
                )
            }
        }

        MapTypeSelector(
            selectedMapType = mapType,
            onMapTypeSelected = { newMapType ->
                mapType = newMapType
                MapTelemetry.trackMapTypeChanged(newMapType)
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )

        RecenterButton(
            onClick = {
                when {
                    hasLocationPermission -> {
                        // Permission already granted, get location and center
                        viewModel.handleMyLocationButtonClick()
                        MapTelemetry.trackRecenterTapped(true)
                    }
                    showDeniedForeverMessage -> {
                        // Show "denied forever" message
                        showDeniedForeverMessage = true
                        MapTelemetry.trackRecenterTapped(false)
                    }
                    else -> {
                        // Check if we should show rationale
                        val shouldShowRationale = locationPermissions.any { permission ->
                            ActivityCompat.shouldShowRequestPermissionRationale(context as ComponentActivity, permission)
                        }
                        
                        if (shouldShowRationale) {
                            showPermissionRationale = true
                        } else {
                            // Request permission directly
                            permissionLauncher.launch(locationPermissions)
                        }
                        MapTelemetry.trackRecenterTapped(false)
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )

        // Show nearest evacuation point banner when available
        nearestLocation?.let { location ->
            distanceMeters?.let { distance ->
                etaMinutes?.let { eta ->
                    EvacuationPointBanner(
                        nearestLocation = location,
                        distanceMeters = distance,
                        etaMinutes = eta,
                        onNavigateClick = {
                            openWalkingDirections(context, location)
                            MapTelemetry.trackEvacuationNavigateClicked(location.name)
                        },
                        onViewOnMapClick = {
                            // Animate camera to nearest location
                            cameraPosition = CameraPosition.builder()
                                .target(location.latLng)
                                .zoom(16f)
                                .build()
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    )
                }
            }
        }

        // Permission rationale dialog
        if (showPermissionRationale) {
            AlertDialog(
                onDismissRequest = { showPermissionRationale = false },
                title = {
                    Text(
                        text = "Location Permission Needed",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    Text(
                        text = "This app needs location access to show your current position on the map and find the nearest evacuation point.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showPermissionRationale = false
                            permissionLauncher.launch(locationPermissions)
                        }
                    ) {
                        Text("Allow")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showPermissionRationale = false }
                    ) {
                        Text("Not now")
                    }
                }
            )
        }

        // Denied forever dialog
        if (showDeniedForeverMessage) {
            AlertDialog(
                onDismissRequest = { showDeniedForeverMessage = false },
                title = {
                    Text(
                        text = "Location Permission Denied",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    Text(
                        text = "Location permission has been permanently denied. You can enable it in the app settings.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeniedForeverMessage = false
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    ) {
                        Text("Open Settings")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeniedForeverMessage = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
        }
    }
}

private fun openWalkingDirections(context: Context, location: MapLocation) {
    val uri = Uri.parse("google.navigation:q=${location.latitude},${location.longitude}&mode=w")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    intent.setPackage("com.google.android.apps.maps")
    
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to web browser if Google Maps app is not available
        val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${location.latitude},${location.longitude}&travelmode=walking")
        val webIntent = Intent(Intent.ACTION_VIEW, webUri)
        context.startActivity(webIntent)
    }
}
