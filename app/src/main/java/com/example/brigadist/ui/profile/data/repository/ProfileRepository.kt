package com.example.brigadist.data.repository

import com.example.brigadist.ui.profile.model.FirebaseUserProfile

interface ProfileRepository {
    fun getUserProfile(
        email: String,
        onSuccess: (FirebaseUserProfile?) -> Unit,
        onError: (String) -> Unit
    )

    fun updateUserProfile(
        profile: FirebaseUserProfile,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
}
