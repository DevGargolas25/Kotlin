# Auto Theme System

This module implements automatic theme switching based on ambient light conditions, providing a seamless user experience that adapts to the user's environment.

## Features

- **Automatic Theme Switching**: Switches between light and dark themes based on ambient light
- **Manual Override**: Users can choose Auto, Light, or Dark modes
- **Hysteresis Logic**: Prevents flickering by using thresholds and sustained time requirements
- **Light Sensor Integration**: Uses device's ambient light sensor (TYPE_LIGHT)
- **Battery Optimization**: Only monitors sensor when app is in foreground and Auto mode is selected
- **Persistence**: User preferences are saved and restored across app restarts
- **Accessibility**: Maintains WCAG AA contrast ratios in both themes

## Architecture

### Core Components

- **ThemeController**: Main controller managing theme state and auto-switching logic
- **AmbientLightMonitor**: Wrapper around Android SensorManager for light sensor
- **ThemePreferences**: Persistent storage for user theme preferences
- **ThemeTelemetry**: Lightweight analytics tracking for theme interactions

### Theme Modes

- **AUTO**: Automatically switches based on ambient light (default)
- **LIGHT**: Always uses light theme
- **DARK**: Always uses dark theme

## Auto Theme Logic

### Thresholds
- **Light Threshold**: ≥ 1000 lux sustained for 2 seconds → Switch to Light theme
- **Dark Threshold**: ≤ 50 lux sustained for 2 seconds → Switch to Dark theme
- **Hysteresis Zone**: 50-1000 lux → Keep current theme (prevents flickering)

### Smoothing
- Uses moving average of last 5 sensor readings to reduce noise
- Prevents rapid theme changes from sensor spikes

### Battery Optimization
- Sensor only active when app is in foreground
- Sensor only active when Auto mode is selected
- Automatic cleanup when app is paused or stopped

## Color System

### Light Theme
- Primary: Teal (#75C1C7)
- Secondary: Green (#60B896)
- Tertiary: Peach (#F1AC89)
- Background: White
- Surface: White
- Error: Red (#E63946)

### Dark Theme
- Primary: Muted Teal (#5BA3A9)
- Secondary: Muted Green (#4A9B7A)
- Tertiary: Warmer Peach (#D4956B)
- Background: Very Dark (#0F1A1B)
- Surface: Very Dark (#0F1A1B)
- Error: Brighter Red (#FF6B6B)

## Usage

### Basic Integration
```kotlin
@Composable
fun MyApp() {
    val context = LocalContext.current
    val themeController = remember { ThemeController(context) }
    val themeState by themeController.themeState.collectAsState()
    
    BrigadistTheme(darkTheme = themeState.isDark) {
        // Your app content
    }
}
```

### Manual Theme Control
```kotlin
// Set theme mode
themeController.setThemeMode(ThemeMode.LIGHT)
themeController.setThemeMode(ThemeMode.DARK)
themeController.setThemeMode(ThemeMode.AUTO)
```

### Lifecycle Management
```kotlin
DisposableEffect(Unit) {
    themeController.onAppResumed()
    onDispose {
        themeController.onAppPaused()
    }
}
```

## Settings Integration

The `ThemeSettingsScreen` provides a user interface for theme selection:

- Radio button selection for Auto/Light/Dark modes
- Descriptive text explaining each mode
- Information card explaining auto theme functionality
- Automatic preference persistence

## Telemetry Events

- `theme_mode_changed`: When user manually changes theme mode
- `theme_auto_switch`: When auto theme switches between light/dark

## Device Compatibility

- **With Light Sensor**: Full auto theme functionality
- **Without Light Sensor**: Falls back to manual theme selection, no crashes
- **Sensor Errors**: Graceful degradation to manual mode

## Performance Considerations

- Sensor sampling rate: SENSOR_DELAY_NORMAL (balanced performance/battery)
- Smoothing buffer: 5 readings maximum
- Debounced recompositions: Only updates UI on threshold crossings
- Memory efficient: Minimal object allocation in sensor callbacks

## Accessibility

- WCAG AA contrast compliance in both themes
- Proper semantic color roles for all UI elements
- SOS emergency colors remain legible in both themes
- Consistent brand identity across theme modes
