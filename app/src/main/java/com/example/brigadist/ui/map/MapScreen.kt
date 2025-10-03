package com.example.brigadist.ui.map

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import com.example.brigadist.Orquestador
import com.example.brigadist.data.MapLocation
import com.example.brigadist.ui.map.components.RecenterButton
import com.example.brigadist.ui.map.components.MapTypeSelector
import com.example.brigadist.ui.map.components.EvacuationPointBanner

@OptIn(ExperimentalPermissionsApi::class)
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

    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Initialize ViewModel with context
    LaunchedEffect(Unit) {
        viewModel.initializeLocationClient(context)
    }

    // Update permission state
    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        hasLocationPermission = locationPermissionsState.allPermissionsGranted
        viewModel.setLocationPermission(hasLocationPermission)
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
                viewModel.handleMyLocationButtonClick()
                MapTelemetry.trackRecenterTapped(hasLocationPermission)
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

        // Show permission rationale card when needed
        if (!hasLocationPermission && locationPermissionsState.shouldShowRationale) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Location permission needed",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Allow location to show the nearest evacuation point.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = { locationPermissionsState.launchMultiplePermissionRequest() }
                        ) {
                            Text("Allow")
                        }
                        TextButton(
                            onClick = { /* Dismiss rationale */ }
                        ) {
                            Text("Not now")
                        }
                    }
                }
            }
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
