# Map Module

This module contains the Google Maps integration for the Brigadist app, replacing the static image with an interactive map.

## Structure

- `MapScreen.kt` - Main map screen with Google Maps integration
- `MapViewModel.kt` - ViewModel for map state management
- `MapTelemetry.kt` - Lightweight telemetry tracking for map interactions
- `components/RecenterButton.kt` - Floating action button for recentering map
- `components/MapTypeSelector.kt` - Map type toggle component

## Features

- **Google Maps Integration**: Real-time interactive map using Maps Compose
- **Location Services**: My Location layer when permission is granted
- **Map Controls**: Recenter FAB and map type selector (Normal/Satellite/Terrain/Hybrid)
- **Permission Handling**: Runtime location permission requests with user-friendly rationale
- **Secure API Key**: API key stored in local.properties (not committed to version control)
- **Theme Integration**: Uses app's Material 3 theme tokens for consistent styling
- **Telemetry**: Lightweight tracking of map interactions

## Security

The Google Maps API key is securely configured:

- **API Key**: Stored in `local.properties` (gitignored)
- **Manifest**: Uses manifest placeholder `${MAPS_API_KEY}`
- **Build**: Gradle reads key from local.properties and injects it
- **No Hardcoding**: Key never appears in source code or committed files

## Permissions

Required permissions for location services:

- `ACCESS_FINE_LOCATION` - For precise location
- `ACCESS_COARSE_LOCATION` - For approximate location

## Usage

The map screen is accessible via the bottom navigation "Map" tab. Features include:

1. **Interactive Map**: Pan, zoom, and explore the map
2. **My Location**: Blue dot shows user's location when permission granted
3. **Recenter Button**: FAB to center map on user's location or default campus location
4. **Map Type Toggle**: Switch between Normal, Satellite, Terrain, and Hybrid views
5. **Permission Rationale**: Non-blocking card explaining location permission benefits

## Default Location

The map defaults to Universidad de los Andes, Bogotá coordinates:
- Latitude: 4.6018
- Longitude: -74.0661
- Zoom: 15f

## Dependencies

- `com.google.maps.android:maps-compose:4.3.0` - Maps Compose library
- `com.google.android.gms:play-services-maps:18.2.0` - Google Maps SDK
- `com.google.android.gms:play-services-location:21.0.1` - Location services
- `com.google.accompanist:accompanist-permissions:0.32.0` - Permission handling

## Telemetry Events

- `map_opened` - When map screen is opened
- `map_recenter_tapped` - When recenter button is tapped
- `map_type_changed` - When map type is changed

## Integration

The map screen is integrated into `MainActivity.kt` and uses the existing navigation pattern. No changes to bottom navigation or routing were required.
