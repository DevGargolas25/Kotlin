package com.example.brigadist.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.brigadist.ui.home.model.VideoCard
import com.example.brigadist.ui.theme.DeepPurple
import com.example.brigadist.ui.theme.MintGreen

@Composable
fun HomeLearnOnYourOwnSection(
    videos: List<VideoCard>,
    onVideoClick: (VideoCard) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp)
            .background(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row {
            Icon(Icons.Default.PlayArrow, null, tint = MintGreen,
                modifier = Modifier.size(20.dp).background(MintGreen.copy(.1f), CircleShape).padding(4.dp))
            Spacer(Modifier.width(8.dp))
            Text("Learn on Your Own", color = DeepPurple)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Watch training videos and safety guides at your own pace.",
            color = DeepPurple.copy(.7f), style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(16.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()).fillMaxWidth()) {
            videos.forEach { v -> HomeVideoCardItem(v) { onVideoClick(v) }; Spacer(Modifier.width(16.dp)) }
        }
    }
}
