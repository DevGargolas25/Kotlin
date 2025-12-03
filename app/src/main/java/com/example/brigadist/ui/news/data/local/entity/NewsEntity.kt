package com.example.brigadist.ui.news.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.brigadist.ui.news.data.local.converter.StringListConverter
import com.example.brigadist.ui.news.model.News

@Entity(tableName = "news")
@TypeConverters(StringListConverter::class)
data class NewsEntity(
    @PrimaryKey val id: String,
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val tags: List<String> = emptyList(),
    val usefulCount: Int = 0,
    val notUsefulCount: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toNews(): News = News(
        id = id,
        title = title,
        description = description,
        imageUrl = imageUrl,
        tags = tags,
        usefulCount = usefulCount,
        notUsefulCount = notUsefulCount
    )

    companion object {
        fun fromNews(news: News): NewsEntity = NewsEntity(
            id = news.id,
            title = news.title,
            description = news.description,
            imageUrl = news.imageUrl,
            tags = news.tags,
            usefulCount = news.usefulCount,
            notUsefulCount = news.notUsefulCount,
            lastUpdated = System.currentTimeMillis()
        )
    }
}
