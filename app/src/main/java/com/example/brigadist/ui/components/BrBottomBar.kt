package com.example.brigadist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.brigadist.R
import com.example.brigadist.ui.theme.DeepPurple
import com.example.brigadist.ui.theme.LightAqua

enum class Destination { Home, Chat, Map, Videos }

/**
 * Bottom bar UI (visual only).
 * Put inside Scaffold(bottomBar = { BrBottomBar(...) })
 * and pair it with [BrSosFab] for the red SOS button.
 */
@Composable
fun BrBottomBar(
    selected: Destination,
    onSelect: (Destination) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 10.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BarItem(
                    painter = rememberVectorPainter(Icons.Outlined.Home),
                    selected = selected == Destination.Home,
                    onClick = { onSelect(Destination.Home) }
                )
                Spacer(Modifier.width(12.dp))
                BarItem(
                    painter = painterResource(R.drawable.ic_forum),
                    selected = selected == Destination.Chat,
                    onClick = { onSelect(Destination.Chat) }
                )
            }

            // 🔴 SOS button inline
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE64646))
                    .clickable { /* TODO: SOS Action */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = "SOS",
                    tint = Color.White
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                BarItem(
                    painter = painterResource(R.drawable.ic_map),
                    selected = selected == Destination.Map,
                    onClick = { onSelect(Destination.Map) }
                )
                Spacer(Modifier.width(12.dp))
                BarItem(
                    painter = painterResource(R.drawable.ic_play_circle),
                    selected = selected == Destination.Videos,
                    onClick = { onSelect(Destination.Videos) }
                )
            }
        }
    }
}
@Composable

private fun BarItem(
    painter: Painter,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) LightAqua.copy(alpha = 0.25f) else Color.Transparent
    val tint = if (selected) DeepPurple else DeepPurple.copy(alpha = 0.7f)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(28.dp)
        )
    }
}


/** Center SOS button (FAB) that floats above the bar. */
@Composable
fun BrSosFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        containerColor = Color(0xFFE64646), // red SOS
        contentColor = Color.White,
        elevation = FloatingActionButtonDefaults.elevation(8.dp)
    ) {
        Icon(Icons.Filled.Warning, contentDescription = "SOS")


    }
}
