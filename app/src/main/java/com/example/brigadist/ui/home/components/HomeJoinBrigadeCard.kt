package com.example.brigadist.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.brigadist.ui.theme.DeepPurple
import com.example.brigadist.ui.theme.TurquoiseBlue

@Composable
fun HomeJoinBrigadeCard(onLearnMore: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().height(120.dp)
                    .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = .2f), Color.Transparent)))
            )
            Column(Modifier.padding(16.dp)) {
                Row {
                    Icon(Icons.Default.Person, null, tint = TurquoiseBlue,
                        modifier = Modifier.clip(CircleShape).background(TurquoiseBlue.copy(.1f)).padding(4.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("aaaaaaaa the Brigade", color = DeepPurple)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "aaaaaaaa part of the student safety team and help keep our campus secure.",
                    color = DeepPurple.copy(.7f), style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = onLearnMore) { Text("Learn More") }
            }
        }
    }
}
