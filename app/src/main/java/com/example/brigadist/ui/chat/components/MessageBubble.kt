package com.example.brigadist.ui.chat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.brigadist.data.AssistantLikesRepository

@Composable
fun MessageBubble(msg: MessageUi, modifier: Modifier = Modifier) {
    val isUser = msg.from == Sender.USER

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            Surface(
                color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = msg.text,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (!isUser) {
            val (voted, setVoted) = remember { mutableStateOf(false) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 4.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = {
                    if (!voted) {
                        AssistantLikesRepository.incrementLike()
                        setVoted(true)
                    }
                }, enabled = !voted) {
                    Icon(imageVector = Icons.Filled.ThumbUp, contentDescription = "Like")
                }
                IconButton(onClick = {
                    if (!voted) {
                        AssistantLikesRepository.incrementDislike()
                        setVoted(true)
                    }
                }, enabled = !voted) {
                    Icon(imageVector = Icons.Filled.ThumbDown, contentDescription = "Dislike")
                }
            }
        }
    }
}
