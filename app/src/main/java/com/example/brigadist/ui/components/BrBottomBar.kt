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
import androidx.compose.ui.unit.sp
import com.example.brigadist.R
import com.example.brigadist.ui.theme.DeepPurple
import com.example.brigadist.ui.theme.LightAqua

enum class Destination { Home, Chat, Map, Videos }

@Composable
fun BrBottomBar(
    selected: Destination,
    onSelect: (Destination) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        shadowElevation = 12.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 24.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                BarItem(
                    painter = rememberVectorPainter(Icons.Outlined.Home),
                    label = "Home",
                    selected = selected == Destination.Home,
                    onClick = { onSelect(Destination.Home) }
                )
                BarItem(
                    painter = painterResource(R.drawable.ic_forum),
                    label = "Chat",
                    selected = selected == Destination.Chat,
                    onClick = { onSelect(Destination.Chat) }
                )
            }

            Spacer(modifier = Modifier.width(80.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                BarItem(
                    painter = painterResource(R.drawable.ic_map),
                    label = "Map",
                    selected = selected == Destination.Map,
                    onClick = { onSelect(Destination.Map) }
                )
                BarItem(
                    painter = painterResource(R.drawable.ic_play_circle),
                    label = "Videos",
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
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) LightAqua.copy(alpha = 0.25f) else Color.Transparent
    val tint = if (selected) DeepPurple else DeepPurple.copy(alpha = 0.6f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            painter = painter,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(30.dp)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = tint
        )
    }
}

@Composable
fun BrSosFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        containerColor = Color(0xFFE64646),
        contentColor = Color.White,
        elevation = FloatingActionButtonDefaults.elevation(10.dp)
    ) {
        Icon(Icons.Filled.Warning, contentDescription = "SOS")
    }
}
