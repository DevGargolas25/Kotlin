package com.example.brigadist.ui.sos

/**
 * Lightweight telemetry for SOS modal interactions.
 * This is a placeholder implementation that can be replaced with actual analytics.
 */
object SosTelemetry {
    
    fun trackSosModalOpened() {
        // TODO: Replace with actual analytics implementation
        // Example: analytics.track("sos_modal_opened")
    }
    
    fun trackSosActionSelected(action: SosAction) {
        // TODO: Replace with actual analytics implementation
        // Example: analytics.track("sos_action_selected", mapOf("action" to action.actionName))
    }
    
    fun trackSosModalClosed() {
        // TODO: Replace with actual analytics implementation
        // Example: analytics.track("sos_modal_closed")
    }
    
    fun trackSosSelectTypeOpened() {
        // TODO: Replace with actual analytics implementation
        // Example: analytics.track("sos_select_type_opened")
    }
    
    fun trackSosTypeSelected(emergencyType: com.example.brigadist.ui.sos.components.EmergencyType) {
        // TODO: Replace with actual analytics implementation
        // Example: analytics.track("sos_type_selected", mapOf("type" to emergencyType.name.lowercase()))
    }
    
    fun trackSosSelectTypeClosed() {
        // TODO: Replace with actual analytics implementation
        // Example: analytics.track("sos_select_type_closed")
    }
}

enum class SosAction(val actionName: String) {
    SEND_EMERGENCY_ALERT("send_emergency_alert"),
    CONTACT_BRIGADE("contact_brigade")
}
