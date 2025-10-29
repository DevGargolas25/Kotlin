package com.example.brigadist.ui.profile

import com.example.brigadist.data.repository.ProfileRepository
import com.example.brigadist.ui.profile.model.FirebaseUserProfile

class ProfilePresenter(
    private val view: ProfileView,
    private val repository: ProfileRepository
) {

    fun loadProfile(email: String) {
        view.showLoading()
        repository.getUserProfile(email,
            onSuccess = { profile ->
                view.hideLoading()
                if (profile != null) view.showProfile(profile)
                else view.showError("No se encontró el perfil del usuario.")
            },
            onError = { msg ->
                view.hideLoading()
                view.showError(msg)
            }
        )
    }

    fun saveProfile(profile: FirebaseUserProfile) {
        view.showLoading()
        repository.updateUserProfile(profile,
            onSuccess = {
                view.hideLoading()
                view.showSuccess("Perfil actualizado correctamente.")
            },
            onError = { msg ->
                view.hideLoading()
                view.showError(msg)
            }
        )
    }
}
