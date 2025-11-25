package com.example.brigadist.ui.profile.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.brigadist.data.repository.FirebaseProfileRepository
import com.example.brigadist.data.repository.ProfileRepository
import com.example.brigadist.ui.profile.data.local.ProfileDatabase
import com.example.brigadist.ui.profile.data.local.entity.UserProfileEntity
import com.example.brigadist.ui.profile.model.FirebaseUserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HybridProfileRepository(context: Context) : ProfileRepository {
    private val firebaseRepository = FirebaseProfileRepository()
    private val localRepository = LocalProfileRepository(context)
    private val context = context
    private val database = ProfileDatabase.getDatabase(context)
    private val dao = database.userProfileDao()
    private val scope = CoroutineScope(Dispatchers.IO)

    private fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    override fun getUserProfile(
        email: String,
        onSuccess: (FirebaseUserProfile?) -> Unit,
        onError: (String) -> Unit
    ) {
        if (isOnline()) {
            // Online: Fetch from Firebase and sync to local database
            firebaseRepository.getUserProfile(
                email = email,
                onSuccess = { profile: FirebaseUserProfile? ->
                    if (profile != null) {
                        // Save to local database for offline access
                        scope.launch {
                            try {
                                val entity = UserProfileEntity.fromFirebaseUserProfile(profile)
                                dao.insertProfile(entity)
                            } catch (e: Exception) {
                                // Log error but don't fail the operation
                            }
                        }
                    }
                    onSuccess(profile)
                },
                onError = { error: String ->
                    // If Firebase fails, try local database as fallback
                    localRepository.getUserProfile(email, onSuccess, onError)
                }
            )
        } else {
            // Offline: Fetch from local SQLite database
            localRepository.getUserProfile(email, onSuccess, onError)
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

        if (isOnline()) {
            // Online: Update Firebase and sync to local database
            firebaseRepository.updateUserProfile(
                profile = profile,
                onSuccess = {
                    // Save to local database for offline access
                    scope.launch {
                        try {
                            val entity = UserProfileEntity.fromFirebaseUserProfile(profile)
                            dao.insertProfile(entity)
                        } catch (e: Exception) {
                            // Log error but don't fail the operation
                        }
                    }
                    onSuccess()
                },
                onError = onError
            )
        } else {
            // Offline: Save to local database only
            localRepository.updateUserProfile(profile, onSuccess, onError)
        }
    }
}

