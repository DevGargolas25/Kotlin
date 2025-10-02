package com.example.brigadist.ui.chat.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun MessageList(
    messages: List<MessageUi>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    
    // Auto-scroll to bottom when new messages are added
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }
    
    LazyColumn(
        modifier = modifier.semantics {
            contentDescription = "Chat messages"
        },
        state = listState,
        reverseLayout = true
    ) {
        // reverseLayout=true expects the items in chronological order,
        // so feed reversed data to show last at bottom.
        items(messages.asReversed(), key = { it.id }) { msg ->
            MessageBubble(msg)
            Spacer(Modifier.height(8.dp))
        }
    }
}
