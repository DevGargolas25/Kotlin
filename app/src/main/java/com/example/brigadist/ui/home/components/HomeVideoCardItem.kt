package com.example.brigadist.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.brigadist.ui.home.model.VideoCard
import com.example.brigadist.ui.theme.DeepPurple
import com.example.brigadist.ui.theme.LightAqua

@Composable
fun HomeVideoCardItem(video: VideoCard,
                      onClick: () -> Unit = {}
) {
    // Soft lilac card like the mock tiles, with rounded corners and subtle elevation
    Surface(
        modifier = Modifier
            .width(240.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = Color(0xFFF4EEF6),           // soft lilac
        tonalElevation = 0.dp,
        shadowElevation = 4.dp
    ) {
        Column(Modifier.padding(14.dp)) {

            // Thumbnail placeholder (rounded, aqua)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(LightAqua),
                contentAlignment = Alignment.Center
            ) {
                // tiny play “triangle” look
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.7f))
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = video.title,
                color = DeepPurple,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = video.duration,
                style = MaterialTheme.typography.labelMedium,
                color = DeepPurple.copy(alpha = 0.7f)
            )
        }
    }
}