# SOS Module

This module contains the emergency assistance UI components for the Brigadist app.

## Structure

- `SosModal.kt` - Main modal component with emergency assistance options
- `SosTelemetry.kt` - Lightweight telemetry tracking for SOS interactions

## Usage

The SOS modal is triggered by the bottom navigation SOS button and provides two main actions:

1. **Send Emergency Alert** - Alerts campus security and brigade members
2. **Contact Brigade** - Connects with the nearest brigade member

## Features

- Material 3 design with consistent theming
- Accessible with proper content descriptions and touch targets
- Responsive layout that works on small and large screens
- Telemetry tracking for analytics (placeholder implementation)
- Proper navigation integration with existing app structure

## Integration

The modal is integrated into `MainActivity.kt` and uses the existing navigation pattern. Both actions currently navigate to the Chat screen as placeholders until specific emergency flows are implemented.
