package com.example.brigadist

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class BrigadistApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Enable Firebase offline persistence BEFORE any database references are created
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        } catch (e: Exception) {
            // Already enabled or error, continue
        }
    }
}

