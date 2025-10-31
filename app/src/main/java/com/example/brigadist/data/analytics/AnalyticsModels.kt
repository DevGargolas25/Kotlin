package com.example.brigadist.data.analytics

/**
 * Base class for analytics summary data
 */
sealed class AnalyticsSummary {
    abstract val updatedAt: Long
}

/**
 * Permission changes summary - tracks location permission grants by date
 */
data class PermissionChangesSummary(
    val data: Map<String, LocationPermissionData>,
    override val updatedAt: Long
) : AnalyticsSummary()

data class LocationPermissionData(
    val location: LocationData
)

data class LocationData(
    val granted: Int
)

/**
 * Permission status summary - tracks total users with permissions granted
 */
data class PermissionStatusSummary(
    val data: Map<String, PermissionStatusData>,
    override val updatedAt: Long
) : AnalyticsSummary()

data class PermissionStatusData(
    val granted: Int
)

/**
 * Profile updates summary - tracks profile update statistics
 */
data class ProfileUpdatesSummary(
    val data: Map<String, ProfileUpdateData>,
    override val updatedAt: Long
) : AnalyticsSummary()

data class ProfileUpdateData(
    val avg: Int,
    val total: Int,
    val users: Int
)

/**
 * Screen views summary - tracks screen view statistics
 */
data class ScreenViewsSummary(
    val data: Map<String, List<ScreenViewData>>,
    override val updatedAt: Long
) : AnalyticsSummary()

data class ScreenViewData(
    val name: String,
    val views: Int
)

/**
 * UI state for analytics data
 */
sealed class AnalyticsUiState {
    object Loading : AnalyticsUiState()
    data class Success<T>(val data: T) : AnalyticsUiState()
    data class Error(val message: String) : AnalyticsUiState()
}

/**
 * Combined analytics data for the dashboard
 */
data class AnalyticsDashboardData(
    val permissionChanges: AnalyticsUiState,
    val permissionStatus: AnalyticsUiState,
    val profileUpdates: AnalyticsUiState,
    val screenViews: AnalyticsUiState
)

/**
 * Helper function to format relative time
 */
fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val minutes = diff / (1000 * 60)
    val hours = diff / (1000 * 60 * 60)
    val days = diff / (1000 * 60 * 60 * 24)

    return when {
        minutes < 1 -> "Updated just now"
        minutes < 60 -> "Updated $minutes min ago"
        hours < 24 -> "Updated $hours hour${if (hours != 1L) "s" else ""} ago"
        else -> "Updated $days day${if (days != 1L) "s" else ""} ago"
    }
}
