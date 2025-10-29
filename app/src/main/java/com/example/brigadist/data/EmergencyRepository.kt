package com.example.brigadist.data

import com.example.brigadist.ui.sos.model.Emergency
import com.google.firebase.database.FirebaseDatabase

class EmergencyRepository {
    private val database = FirebaseDatabase.getInstance().getReference("Emergency")

    /**
     * Creates a new emergency record in Firebase.
     * Generates a unique emergencyID using push().key converted to Long.
     * 
     * @param emergency The emergency object to create (emergencyID will be generated if not set)
     * @param onSuccess Callback invoked when emergency is successfully created
     * @param onError Callback invoked with error message if creation fails
     */
    fun createEmergency(
        emergency: Emergency,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Generate unique emergencyID if not already set
        val emergencyWithId = if (emergency.emergencyID == 0L) {
            val pushKey = database.push().key
            val generatedId = pushKey?.takeLast(8)?.toLongOrNull() ?: System.currentTimeMillis() % 100000000
            emergency.copy(emergencyID = generatedId)
        } else {
            emergency
        }

        // Write emergency to Firebase
        database.push().setValue(emergencyWithId)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Error al crear la emergencia")
            }
    }
}

