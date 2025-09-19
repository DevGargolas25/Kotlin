package com.example.brigadist.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.brigadist.ui.home.model.VideoCard
import com.example.brigadist.ui.theme.DeepPurple
import com.example.brigadist.ui.theme.LightAqua

@Composable
fun HomeVideoCardItem(video: VideoCard, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.width(208.dp).height(180.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier.fillMaxWidth().height(80.dp).background(LightAqua),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(video.title, color = DeepPurple, maxLines = 2)
            Text(video.duration, style = MaterialTheme.typography.bodySmall)
        }
    }
}
