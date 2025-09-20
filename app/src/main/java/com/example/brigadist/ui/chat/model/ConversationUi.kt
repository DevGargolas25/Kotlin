// ui/chat/model/ConversationUi.kt
package com.example.brigadist.ui.chat.model

data class ConversationUi(
    val id: Int,
    val name: String,
    val lastMessage: String,
    val time: String,      // e.g., "10:32 AM" or "Yesterday"
    val unreadCount: Int = 0
)
