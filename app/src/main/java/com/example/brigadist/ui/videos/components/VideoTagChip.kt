package com.example.brigadist.ui.videos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.brigadist.ui.theme.LightAqua
import com.example.brigadist.ui.theme.DeepPurple

@Composable
fun VideoTagChip(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(LightAqua.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text, color = DeepPurple, style = MaterialTheme.typography.labelMedium)
    }
}
