package com.example.brigadist.ui.chat.components

data class MessageUi(
    val id: Long,
    val from: Sender,
    val text: String
)

enum class Sender { USER, ASSISTANT }
