package com.example.brigadist.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest

/**
 * Manages offline credentials for authentication when there's no internet connection.
 * Stores a secure hash of user credentials for offline validation.
 */
class OfflineCredentialsManager(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    /**
     * Stores offline credentials for a user.
     * Creates a secure hash of email + password.
     */
    fun saveOfflineCredentials(email: String, password: String, userType: String? = null) {
        val credentialHash = hashCredentials(email, password)
        sharedPreferences.edit().apply {
            putString(KEY_EMAIL, email)
            putString(KEY_CREDENTIAL_HASH, credentialHash)
            putBoolean(KEY_HAS_OFFLINE_CREDENTIALS, true)
            if (userType != null) {
                putString(KEY_USER_TYPE, userType)
            }
            apply()
        }
    }
    
    /**
     * Validates offline credentials against stored hash.
     * Returns true if credentials match.
     */
    fun validateOfflineCredentials(email: String, password: String): Boolean {
        val storedEmail = sharedPreferences.getString(KEY_EMAIL, null)
        val storedHash = sharedPreferences.getString(KEY_CREDENTIAL_HASH, null)
        
        if (storedEmail == null || storedHash == null) {
            return false
        }
        
        // Verify email matches
        if (storedEmail != email) {
            return false
        }
        
        // Verify password hash matches
        val inputHash = hashCredentials(email, password)
        return inputHash == storedHash
    }
    
    /**
     * Checks if offline credentials are set up.
     */
    fun hasOfflineCredentials(): Boolean {
        return sharedPreferences.getBoolean(KEY_HAS_OFFLINE_CREDENTIALS, false)
    }
    
    /**
     * Gets the stored email for offline authentication.
     */
    fun getStoredEmail(): String? {
        return sharedPreferences.getString(KEY_EMAIL, null)
    }
    
    /**
     * Gets the stored user type for offline authentication.
     */
    fun getStoredUserType(): String? {
        return sharedPreferences.getString(KEY_USER_TYPE, null)
    }
    
    /**
     * Saves the user type for offline authentication.
     */
    fun saveUserType(userType: String) {
        sharedPreferences.edit().apply {
            putString(KEY_USER_TYPE, userType)
            apply()
        }
    }
    
    /**
     * Clears all offline credentials.
     */
    fun clearOfflineCredentials() {
        sharedPreferences.edit().clear().apply()
    }
    
    /**
     * Creates a secure hash of the credentials using SHA-256.
     */
    private fun hashCredentials(email: String, password: String): String {
        val input = "$email:$password"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
    
    companion object {
        private const val PREFS_NAME = "offline_credentials"
        private const val KEY_EMAIL = "email"
        private const val KEY_CREDENTIAL_HASH = "credential_hash"
        private const val KEY_HAS_OFFLINE_CREDENTIALS = "has_offline_credentials"
        private const val KEY_USER_TYPE = "user_type"
    }
}

