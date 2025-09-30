package com.example.brigadist.ui.theme

import androidx.compose.ui.graphics.Color

// Core palette
val TealPrimary       = Color(0xFF75C1C7) // headers, primary accents
val TealContainer     = Color(0xFFEAF5F6) // light teal container/background
val GreenSecondary     = Color(0xFF60B896) // positive accents, user bubbles
val PeachTertiary      = Color(0xFFF1AC89) // warm accent (optional)
val AquaSoftSurface    = Color(0xFFF7FBFC) // soft surface background
val DeepPurpleText     = Color(0xFF4A2951) // body text
val OutlineAqua        = Color(0xFF99D2D2) // borders/dividers (use with alpha)
val ErrorRed           = Color(0xFFE63946) // SOS/error

// SOS Alert Category Colors
// Fire Alert - Warm/Coral family derived from PeachTertiary
val AlertFireAccent = PeachTertiary
val AlertFireContainer = Color(0xFFF5E6E0) // Softened peach container
val OnAlertFireContainer = Color(0xFF8B4513) // Dark brown for contrast

// Earthquake Alert - Teal/Blue-green family derived from TealPrimary
val AlertEarthquakeAccent = TealPrimary
val AlertEarthquakeContainer = TealContainer
val OnAlertEarthquakeContainer = DeepPurpleText

// Medical Alert - Health/Green family derived from GreenSecondary
val AlertMedicalAccent = GreenSecondary
val AlertMedicalContainer = Color(0xFFE6F3EE) // Softened green container
val OnAlertMedicalContainer = DeepPurpleText

// Legacy aliases for backward compatibility during migration
val SoftWhite = AquaSoftSurface
val LightAqua = OutlineAqua
val DeepPurple = DeepPurpleText
val TurquoiseBlue = TealPrimary
val MintGreen = GreenSecondary

// Dark Theme Colors
// Core dark palette - maintaining brand identity while ensuring accessibility
val TealPrimaryDark = Color(0xFF5BA3A9) // Slightly muted teal for dark backgrounds
val TealContainerDark = Color(0xFF1A3A3D) // Dark teal container
val GreenSecondaryDark = Color(0xFF4A9B7A) // Muted green for dark mode
val PeachTertiaryDark = Color(0xFFD4956B) // Warmer peach for dark mode
val AquaSoftSurfaceDark = Color(0xFF0F1A1B) // Very dark surface
val DeepPurpleTextDark = Color(0xFFE8D5ED) // Light purple text for dark backgrounds
val OutlineAquaDark = Color(0xFF4A7A7A) // Muted aqua outline
val ErrorRedDark = Color(0xFFFF6B6B) // Brighter red for dark mode

// SOS Alert Category Colors - Dark Theme
val AlertFireAccentDark = PeachTertiaryDark
val AlertFireContainerDark = Color(0xFF2D1A0F) // Dark peach container
val OnAlertFireContainerDark = Color(0xFFFFB366) // Light orange for contrast

val AlertEarthquakeAccentDark = TealPrimaryDark
val AlertEarthquakeContainerDark = TealContainerDark
val OnAlertEarthquakeContainerDark = DeepPurpleTextDark

val AlertMedicalAccentDark = GreenSecondaryDark
val AlertMedicalContainerDark = Color(0xFF1A2D25) // Dark green container
val OnAlertMedicalContainerDark = DeepPurpleTextDark
