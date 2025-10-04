package com.example.brigadist.data

import com.google.firebase.database.*
import kotlinx.coroutines.tasks.await

/**
 * Adapter ligero para leer/actualizar datos de "User" en Realtime Database.
 *
 * Busca el nodo bajo "User" usando orderByChild("email").equalTo(email)
 * y devuelve la clave del nodo (ej. "U001") junto con los datos.
 */
class FirebaseUserAdapter {

    private val rootRef: DatabaseReference = FirebaseDatabase.getInstance().reference

    data class FoundUser(
        val key: String,
        val data: Map<String, Any?> = emptyMap()
    )

    /**
     * Busca el usuario por correo. Retorna null si no lo encuentra.
     * Implementado con callback porque ValueEventListener es la forma nativa.
     */
    fun findUserByEmail(email: String, onResult: (FoundUser?) -> Unit, onError: ((DatabaseError) -> Unit)? = null) {
        val usersRef = rootRef.child("User")
        val q = usersRef.orderByChild("email").equalTo(email)
        q.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    onResult(null)
                    return
                }
                // Se espera un solo match (emails únicos). Tomamos el primer hijo.
                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    val map = mutableMapOf<String, Any?>()
                    for (field in child.children) {
                        map[field.key ?: ""] = field.value
                    }
                    onResult(FoundUser(key = key, data = map))
                    return
                }
                onResult(null)
            }

            override fun onCancelled(error: DatabaseError) {
                onError?.invoke(error)
            }
        })
    }

    /**
     * Actualiza (updateChildren) los campos provistos en el usuario con clave "userKey".
     * onComplete recibe (success: Boolean, error: String?)
     */
    fun updateUserFields(userKey: String, fields: Map<String, Any?>, onComplete: (Boolean, String?) -> Unit) {
        val userRef = rootRef.child("User").child(userKey)
        // Filtrar nulls (Realtime Database no permite null en updateChildren bien)
        val filtered = fields.mapValues { it.value }
        userRef.updateChildren(filtered).addOnSuccessListener {
            onComplete(true, null)
        }.addOnFailureListener { ex ->
            onComplete(false, ex.message)
        }
    }
}
