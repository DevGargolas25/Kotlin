package com.example.brigadist.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.brigadist.data.local.dao.EmergencyDao
import com.example.brigadist.data.local.entity.EmergencyEntity

@Database(
    entities = [EmergencyEntity::class],
    version = 1,
    exportSchema = false
)
abstract class EmergencyDatabase : RoomDatabase() {
    abstract fun emergencyDao(): EmergencyDao
    
    companion object {
        @Volatile
        private var INSTANCE: EmergencyDatabase? = null
        
        fun getDatabase(context: Context): EmergencyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EmergencyDatabase::class.java,
                    "emergency_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

