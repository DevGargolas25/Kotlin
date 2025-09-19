package com.example.brigadist.ui.home.model
data class VideoCard(
    val title: String,
    val duration: String,
    val category: String,
    val views: String,
    val description: String
)

object HomeSamples {
    val notifications = listOf(
        "Campus safety drill scheduled for tomorrow at 2 PM",
        "New emergency exits have been installed in the library",
        "Weather alert: Strong winds expected this afternoon"
    )

    val videos = listOf(
        VideoCard("Campus Emergency Procedures", "5:24", "Emergency", "2.3k views","..."),
        VideoCard("First Aid Basics for Students", "8:15", "Medical", "1.8k views","...")
    )
}
