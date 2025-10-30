package com.example.brigadist.ui.sos.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Emergency(
    var EmerResquestTime: Long = 0,
    var assignedBrigadistId: String = "",
    var createdAt: Long = 0,
    var date_time: String = "",
    var emerType: String = "",
    var emergencyID: Long = 0,
    var location: String = "",
    var secondsResponse: Int = 5,
    var seconds_response: Int = 5,
    var updatedAt: Long = 0,
    var userId: String = ""
)

