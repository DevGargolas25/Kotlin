package com.example.brigadist.ui.news.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun VoteSection(
    usefulCount: Int,
    notUsefulCount: Int,
    hasVoted: Boolean,
    onVote: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
        Text(text = "Was this news helpful?", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(12.dp))
        Row {
            Button(
                onClick = { onVote(true) },
                enabled = !hasVoted,
                modifier = Modifier.weight(1f)
            ) {
                Text("Helpful ($usefulCount)")
            }

            Spacer(Modifier.width(12.dp))

            Button(
                onClick = { onVote(false) },
                enabled = !hasVoted,
                modifier = Modifier.weight(1f)
            ) {
                Text("Not helpful ($notUsefulCount)")
            }
        }

        if (hasVoted) {
            Spacer(Modifier.height(8.dp))
            Text(text = "Thanks for your feedback.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
