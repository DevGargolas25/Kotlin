package com.example.brigadist.ui.videos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.brigadist.ui.theme.DeepPurple
import com.example.brigadist.ui.theme.LightAqua

@Composable
fun CategoryChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAll = text.equals("All", ignoreCase = true)

    if (isAll) {
        // Circular "All" chip (filled aqua with white text)
        Box(
            modifier = modifier
                .size(40.dp)
                .background(LightAqua, CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "All", color = Color.White, style = MaterialTheme.typography.labelLarge)
        }
    } else {
        // Rounded pill for the rest
        val bg = if (selected) LightAqua.copy(alpha = 0.35f) else Color(0xFFEFF4F4)
        val fg = DeepPurple

        Box(
            modifier = modifier
                .background(bg, RoundedCornerShape(24.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = text, color = fg, style = MaterialTheme.typography.labelLarge)
        }
    }
}
