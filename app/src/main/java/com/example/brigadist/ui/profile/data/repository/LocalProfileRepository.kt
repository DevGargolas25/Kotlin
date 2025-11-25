package com.example.brigadist.ui.profile.data.repository

import android.content.Context
import com.example.brigadist.data.repository.ProfileRepository
import com.example.brigadist.ui.profile.data.local.ProfileDatabase
import com.example.brigadist.ui.profile.data.local.entity.UserProfileEntity
import com.example.brigadist.ui.profile.model.FirebaseUserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocalProfileRepository(context: Context) : ProfileRepository {
    private val database = ProfileDatabase.getDatabase(context)
    private val dao = database.userProfileDao()
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun getUserProfile(
        email: String,
        onSuccess: (FirebaseUserProfile?) -> Unit,
        onError: (String) -> Unit
    ) {
        scope.launch {
            try {
                val entity = dao.getProfileByEmail(email)
                val profile = entity?.toFirebaseUserProfile()
                onSuccess(profile)
            } catch (e: Exception) {
                onError(e.message ?: "Error reading from local database")
            }
        }
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

        scope.launch {
            try {
                val entity = UserProfileEntity.fromFirebaseUserProfile(profile)
                dao.insertProfile(entity)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error saving to local database")
            }
        }
    }
}

