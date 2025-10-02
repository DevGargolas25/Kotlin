package com.example.brigadist.auth

import com.auth0.jwt.JWT
import com.auth0.android.result.Credentials

data class User(
    val id: String,
    val name: String,
    val email: String,
    val picture: String,
)

fun credentialsToUser(credentials: Credentials): User? {
    // Decode the ID token to get the user's unique ID (the 'sub' claim)
    val jwt = com.auth0.jwt.JWT.decode(credentials.idToken)
    val id = jwt.subject
    
    // Get other profile info from the UserProfile object
    val name = credentials.user.name
    val email = credentials.user.email
    val picture = credentials.user.pictureURL

    if (id != null && name != null && email != null && picture != null) {
        return User(id, name, email, picture)
    }
    return null
}