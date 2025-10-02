package com.example.brigadist.ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.brigadist.Orquestador
import com.example.brigadist.R
import com.example.brigadist.ui.chat.components.ChatTopBar
import com.example.brigadist.ui.chat.components.ConversationList

@Composable
fun ChatScreen(
    orquestador: Orquestador,
    onOpenConversation: () -> Unit = {}
) {
    var query by remember { mutableStateOf("") }

    val conversations = orquestador.getConversations()

    Column(modifier = Modifier.fillMaxSize()) {
        ChatTopBar(
            search = query,
            onSearchChange = { query = it },
            headerColor = MaterialTheme.colorScheme.primary
        )

        ConversationList(
            items = conversations.filter {
                query.isBlank() || it.name.contains(query, ignoreCase = true)
            },
            iconById = { convo ->
                when (convo.id) {
                    1 -> R.drawable.ic_assistant
                    2 -> R.drawable.ic_group
                    3 -> R.drawable.ic_bubble
                    else -> null
                }
            },
            colorById = { convo ->
                when (convo.id) {
                    1 -> Color(0xFF75C0BE) // teal (assistant)
                    2 -> Color(0xFF7CCB9E) // green (team)
                    3 -> Color(0xFFF7BFA3) // peach (alerts)
                    else -> MaterialTheme.colorScheme.secondary
                }
            },
            onClick = { _ -> onOpenConversation() }
        )
    }
}
