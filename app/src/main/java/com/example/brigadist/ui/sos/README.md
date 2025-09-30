# SOS Module

This module contains the emergency assistance UI components for the Brigadist app.

## Structure

- `SosModal.kt` - Step 1: Main modal component with emergency assistance options
- `SosSelectTypeModal.kt` - Step 2: Emergency type selection modal
- `SosTelemetry.kt` - Lightweight telemetry tracking for SOS interactions
- `components/SosHeader.kt` - Red header band component with alert icon and title
- `components/SosTypeRow.kt` - Reusable row component for emergency type selection

## Usage Flow

### Step 1: Initial SOS Modal
The SOS modal is triggered by the bottom navigation SOS button and provides two main actions:

1. **Send Emergency Alert** - Proceeds to Step 2 (Emergency Type Selection)
2. **Contact Brigade** - Connects with the nearest brigade member

### Step 2: Emergency Type Selection
When "Send Emergency Alert" is selected, the user is presented with three emergency types:

1. **Fire Alert** - Report fire emergency or smoke detection
2. **Earthquake Alert** - Report seismic activity or structural damage  
3. **Medical Alert** - Report medical emergency or injury

## Features

- Material 3 design with consistent theming
- **Red header bands** using `MaterialTheme.colorScheme.error` for emergency context
- **Two-step modal flow** with proper navigation between steps
- Accessible with proper content descriptions and touch targets
- Responsive layout that works on small and large screens
- Telemetry tracking for analytics (placeholder implementation)
- Proper navigation integration with existing app structure

## Visual Design

### Step 1 Modal
- **Header**: Red background with white text and alert icon in circular chip
- **Content**: White background with two action buttons and footer note
- **Divider**: Subtle separator between header and content areas
- **Close Button**: Positioned in top-right of red header with proper contrast

### Step 2 Modal
- **Header**: Red background with "Select Emergency Type" title and subtitle
- **Content**: Three emergency type rows with appropriate icons
- **Footer**: Safety note about immediate notification
- **Navigation**: Each type selection proceeds to next step (placeholder)

## Integration

The modal flow is integrated into `MainActivity.kt` using the existing navigation pattern:
- Step 1: `showSosModal` state
- Step 2: `showSosSelectTypeModal` state
- Both steps currently navigate to Chat screen as placeholders until specific emergency flows are implemented
