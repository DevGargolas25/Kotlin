package com.example.brigadist.data.repository

import com.example.brigadist.ui.profile.model.FirebaseUserProfile
import com.google.firebase.database.*

class FirebaseProfileRepository : ProfileRepository {
    private val database = FirebaseDatabase.getInstance().reference.child("User")

    override fun getUserProfile(
        email: String,
        onSuccess: (FirebaseUserProfile?) -> Unit,
        onError: (String) -> Unit
    ) {
        database.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userSnapshot = snapshot.children.firstOrNull()
                    val profile = userSnapshot?.getValue(FirebaseUserProfile::class.java)
                    onSuccess(profile)
                }

                override fun onCancelled(error: DatabaseError) {
                    onError(error.message)
                }
            })
    }

    override fun updateUserProfile(
        profile: FirebaseUserProfile,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (profile.email.isBlank()) {
            onError("No se puede actualizar: email vacío.")
            return
        }

        database.orderByChild("email").equalTo(profile.email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userSnapshot = snapshot.children.firstOrNull()
                    if (userSnapshot != null) {
                        userSnapshot.ref.setValue(profile)
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { onError(it.message ?: "Error al actualizar") }
                    } else {
                        onError("Usuario no encontrado")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    onError(error.message)
                }
            })
    }
}
