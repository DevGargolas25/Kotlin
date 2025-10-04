package com.example.brigadist.ui.profile

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadist.data.FirebaseUserAdapter
import kotlinx.coroutines.launch

/**
 * ViewModel para ProfileScreen.
 * - loadProfile(email) buscará el nodo bajo /User que coincida con el email.
 * - expone estados simples para Compose (loading, error, user data map, userKey).
 *
 * Nota: el "perfil" se mantiene como Map<String, Any?> para ser flexible con los campos
 * presentes en tu JSON. Si prefieres tipar todo, podemos mapear a una data class.
 */
class ProfileViewModel : ViewModel() {

    private val adapter = FirebaseUserAdapter()

    // estado observable por Compose
    val loading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    val userKey = mutableStateOf<String?>(null)             // ej. "U001"
    val userData = mutableStateOf<Map<String, Any?>>(emptyMap()) // campos del usuario

    /**
     * Carga el perfil buscando por email en /User.
     */
    fun loadProfile(email: String) {
        loading.value = true
        errorMessage.value = null

        adapter.findUserByEmail(email, onResult = { found ->
            if (found == null) {
                loading.value = false
                errorMessage.value = "Usuario no encontrado en la DB"
                userKey.value = null
                userData.value = emptyMap()
            } else {
                userKey.value = found.key
                userData.value = found.data
                loading.value = false
            }
        }, onError = { dbError ->
            loading.value = false
            errorMessage.value = "Error DB: ${dbError.message}"
        })
    }

    /**
     * Actualiza los campos indicados en la DB. Recibe un Map con los campos a actualizar.
     * Actualiza también el estado local userData con los valores nuevos (si success).
     */
    fun updateProfileFields(fields: Map<String, Any?>, onComplete: (Boolean, String?) -> Unit) {
        val key = userKey.value
        if (key == null) {
            onComplete(false, "Clave de usuario no establecida")
            return
        }

        loading.value = true
        adapter.updateUserFields(key, fields) { success, err ->
            loading.value = false
            if (success) {
                // merge local map
                val newMap = userData.value.toMutableMap()
                for ((k, v) in fields) {
                    newMap[k] = v
                }
                userData.value = newMap
                onComplete(true, null)
            } else {
                onComplete(false, err)
            }
        }
    }
}
