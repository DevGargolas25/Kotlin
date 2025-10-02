package com.example.brigadist.ui.chat.model

data class ConversationUi(
    val id: Int,
    val name: String,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int
)

