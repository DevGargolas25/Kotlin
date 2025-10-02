package com.example.brigadist.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import com.example.brigadist.R
import com.example.brigadist.ui.chat.components.*
import kotlinx.coroutines.delay

@Composable
fun ChatScreen() {
    // Single-thread chat state
    val messages = remember { mutableStateListOf<MessageUi>() }
    val isTyping = remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val lastUserMessageId = remember { mutableStateOf<Long?>(null) }
    
    // Auto-scroll to bottom when new messages are added
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }
    
    // Handle typing simulation when user sends a message
    LaunchedEffect(lastUserMessageId.value) {
        lastUserMessageId.value?.let { messageId ->
            isTyping.value = true
            delay(1500) // 1.5 second delay
            isTyping.value = false
            
            // Add assistant reply
            messages += MessageUi(
                id = messageId + 1L,
                from = Sender.ASSISTANT,
                text = "Got it. I'll guide you step by step."
            )
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Message list consumes available vertical space
        Box(modifier = Modifier.weight(1f)) {
            if (messages.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics {
                            contentDescription = "Chat messages"
                        },
                    state = listState,
                    reverseLayout = true,
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    // Show typing indicator if active
                    if (isTyping.value) {
                        item {
                            Row {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                                ) {
                                    TypingIndicator()
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                    
                    // reverseLayout=true expects the items in chronological order,
                    // so feed reversed data to show last at bottom.
                    items(messages.asReversed(), key = { it.id }) { msg ->
                        MessageBubble(msg)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        // Composer at the bottom
        ComposerBar(
            onSend = { text ->
                val nextId = (messages.lastOrNull()?.id ?: 0L) + 1L
                // Append user message
                messages += MessageUi(id = nextId, from = Sender.USER, text = text)
                
                // Trigger typing simulation
                lastUserMessageId.value = nextId
            }
        )
    }
}