package com.example.brigadist.ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import com.example.brigadist.R
import com.example.brigadist.ui.chat.components.ChatTopBar
import com.example.brigadist.ui.chat.components.ConversationList
import com.example.brigadist.ui.chat.model.ConversationUi

@Composable
fun ChatScreen() { //asdfsadf
    var query by remember { mutableStateOf("") }

    // Sample data like your screenshot
    val conversations = listOf(
        ConversationUi(1, "Brigade Assistant",
            "The main assembly points are: Main Campus: Front park…", "10:32 AM", 2),
        ConversationUi(2, "Brigade Team",
            "Meeting tonight at 7 PM in room 203. Please confirm y…", "9:45 AM", 0),
        ConversationUi(3, "Brigade Alerts",
            "Weather alert: Strong winds expected this afternoon. St…", "Yesterday", 1),
    )

    Column(modifier = Modifier.fillMaxSize()) {
        ChatTopBar(
            search = query,
            onSearchChange = { query = it },
            headerColor = MaterialTheme.colorScheme.secondary
        )

        ConversationList(
            items = conversations.filter {
                query.isBlank() || it.name.contains(query, ignoreCase = true)
            },
            iconById = { convo ->
                when (convo.id) {
                    1 -> R.drawable.ic_assistant   // add these vector assets or change names
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
            onClick = { /* TODO: navigate to conversation detail */ }
        )
    }
}
