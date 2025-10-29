package com.example.brigadist.ui.profile

import com.example.brigadist.ui.profile.model.FirebaseUserProfile

interface ProfileView {
    fun showLoading()
    fun hideLoading()
    fun showProfile(profile: FirebaseUserProfile)
    fun showError(message: String)
    fun showSuccess(message: String)
}
