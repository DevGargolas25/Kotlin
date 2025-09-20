package com.example.brigadist.ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.brigadist.ui.chat.components.ChatTopBar

@Composable
fun ChatScreen() {
    var query by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        ChatTopBar(
            search = query,
            onSearchChange = { query = it },
            headerColor = MaterialTheme.colorScheme.secondary // replace if you use a custom turquoise
        )

        // TODO: next iteration — list of conversations below
    }
}
