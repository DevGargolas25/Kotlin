package com.example.brigadist.ui.map

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import com.example.brigadist.Orquestador
import com.example.brigadist.ui.map.components.RecenterButton
import com.example.brigadist.ui.map.components.MapTypeSelector

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(orquestador: Orquestador) {
    val context = LocalContext.current
    var cameraPosition by remember { mutableStateOf(orquestador.getDefaultCameraPosition()) }
    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }

    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        hasLocationPermission = locationPermissionsState.allPermissionsGranted
    }

    LaunchedEffect(Unit) {
        MapTelemetry.trackMapOpened(hasLocationPermission, mapType)
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        userLocation = LatLng(it.latitude, it.longitude)
                    }
                }
            } catch (e: SecurityException) {
                hasLocationPermission = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = CameraPositionState(
                position = cameraPosition
            ),
            properties = MapProperties(
                mapType = mapType,
                isMyLocationEnabled = hasLocationPermission
            )
        )

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
                if (hasLocationPermission && userLocation != null) {
                    cameraPosition = CameraPosition.builder().target(userLocation!!).zoom(16f).build()
                    MapTelemetry.trackRecenterTapped(true)
                } else {
                    cameraPosition = orquestador.getDefaultCameraPosition()
                    MapTelemetry.trackRecenterTapped(false)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )

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
                        text = "Location Permission",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Allow location access to show your position on the map and enable recenter functionality.",
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
                            Text("Grant Permission")
                        }
                        TextButton(
                            onClick = { /* Dismiss rationale */ }
                        ) {
                            Text("Not Now")
                        }
                    }
                }
            }
        }
    }
}
