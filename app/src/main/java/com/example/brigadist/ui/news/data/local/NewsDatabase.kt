package com.example.brigadist.ui.news.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.brigadist.ui.news.data.local.converter.StringListConverter
import com.example.brigadist.ui.news.data.local.dao.NewsDao
import com.example.brigadist.ui.news.data.local.entity.NewsEntity

@Database(entities = [NewsEntity::class], version = 2, exportSchema = false)
@TypeConverters(StringListConverter::class)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun newsDao(): NewsDao

    companion object {
        @Volatile private var INSTANCE: NewsDatabase? = null

        fun getDatabase(context: Context): NewsDatabase {
            return INSTANCE ?: synchronized(this) {
                val inst = Room.databaseBuilder(
                    context.applicationContext,
                    NewsDatabase::class.java,
                    "news_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = inst
                inst
            }
        }
    }
}
