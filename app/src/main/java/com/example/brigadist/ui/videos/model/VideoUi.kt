package com.example.brigadist.ui.videos.model

data class VideoUi(
    val id: String,
    val title: String,
    val durationSec: Int,
    val tags: List<String>,      // e.g. ["Emergency", "Safety"]
    val author: String,          // e.g. "Student Brigade"
    val viewsText: String,       // e.g. "2.3k views"
    val ageText: String,          // e.g. "2 weeks ago"
    val description: String
    )

// mm:ss
fun Int.formatAsDuration(): String {
    val m = this / 60
    val s = this % 60
    return "%d:%02d".format(m, s)
}

// --- mock data for the list
fun mockVideos(): List<VideoUi> = listOf(
    VideoUi(
        id = "1",
        title = "Campus Emergency Procedures",
        durationSec = 5 * 60 + 24,
        tags = listOf("Emergency", "Safety"),
        author = "Student Brigade",
        viewsText = "2.3k views",
        ageText = "2 weeks ago",
        description = "2 weeks ago"
    ),
    VideoUi(//update
        id = "2",
        title = "First Aid Basics for Students",
        durationSec = 8 * 60 + 15,
        tags = listOf("Medical", "Training"),
        author = "Student Brigade",
        viewsText = "1.8k views",
        ageText = "1 month ago",
        description = "2 weeks ago"
    ),
    VideoUi(
        id = "3",
        title = "Student Brigade Orientation",
        durationSec = 12 * 60 + 30,
        tags = listOf("Training", "Safety"),
        author = "Student Brigade",
        viewsText = "912 views",
        ageText = "3 months ago",
        description = "2 weeks ago"
    )
)
