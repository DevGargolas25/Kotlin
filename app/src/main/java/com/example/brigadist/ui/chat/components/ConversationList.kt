package com.example.brigadist.ui.chat.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.brigadist.ui.chat.model.ConversationUi

@Composable
fun ConversationList(
    items: List<ConversationUi>,
    modifier: Modifier = Modifier,
    iconById: (ConversationUi) -> Int?,
    colorById: @Composable (ConversationUi) -> Color,   // ← make it composable
    onClick: (ConversationUi) -> Unit
) {
    LazyColumn(modifier = modifier) {
        items(items, key = { it.id }) { convo ->
            ConversationRow(
                conversation = convo,
                onClick = { onClick(convo) },
                avatarIcon = iconById(convo),
                avatarColor = colorById(convo)        // ← safe to call
            )
        }
    }
}

