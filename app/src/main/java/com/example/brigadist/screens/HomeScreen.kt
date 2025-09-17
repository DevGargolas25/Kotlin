package com.example.brigadist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// Paleta de colores definida en la descripción
private val SoftWhite = Color(0xFFF7FBFC)
private val LightAqua = Color(0xFF99D2D2)
private val DeepPurple = Color(0xFF4A2951)
private val TurquoiseBlue = Color(0xFF75C1C7)
private val MintGreen = Color(0xFF60B896)

// Datos de prueba
private val notifications = listOf(
    "Campus safety drill scheduled for tomorrow at 2 PM",
    "New emergency exits have been installed in the library",
    "Weather alert: Strong winds expected this afternoon",
    "Student Brigade meeting tonight at 7 PM in room 203",
    "Emergency contact information has been updated",
    "Fire safety equipment inspection completed successfully",
    "Campus security patrol has been increased during evening hours"
)

data class VideoCard(
    val title: String,
    val duration: String,
    val category: String,
    val views: String,
    val description: String
)

private val videos = listOf(
    VideoCard("Campus Emergency Procedures", "5:24", "Emergency", "2.3k views",
        "Learn essential emergency procedures for campus safety..."),
    VideoCard("First Aid Basics for Students", "8:15", "Medical", "1.8k views",
        "Essential first aid techniques every student should know..."),
    VideoCard("Student Brigade Orientation", "12:30", "Training", "954 views",
        "Complete orientation for new Student Brigade members..."),
    VideoCard("Fire Safety on Campus", "6:45", "Safety", "3.1k views",
        "Comprehensive fire safety guide for campus buildings..."),
    VideoCard("Mental Health Resources", "9:18", "Medical", "1.2k views",
        "Important information about mental health resources...")
)

@Composable
fun HomeScreen() {
    var showMenu by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    var currentNotificationIndex by remember { mutableStateOf(0) }


    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(10000)
            currentNotificationIndex = (currentNotificationIndex + 1) % notifications.size
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftWhite)
            .padding(bottom = 80.dp) // pb-20
    ) {

        NotificationBar(
            text = notifications[currentNotificationIndex],
            onBellClick = { showNotifications = true },
            onMenuClick = { showMenu = true }
        )


        Text(
            text = "Hi John!",
            style = MaterialTheme.typography.headlineSmall,
            color = DeepPurple,
            modifier = Modifier.padding(top = 8.dp, start = 16.dp)
        )

        Spacer(Modifier.height(16.dp))


        JoinBrigadeCard()

        Spacer(Modifier.height(16.dp))


        LearnOnYourOwnSection()
    }


    if (showMenu) {
        AlertDialog(
            onDismissRequest = { showMenu = false },
            confirmButton = {},
            title = { Text("Profile & Settings") },
            text = { Text("Manage your account and preferences") }
        )
    }


    if (showNotifications) {
        AlertDialog(
            onDismissRequest = { showNotifications = false },
            confirmButton = {},
            title = { Text("All Notifications") },
            text = {
                Column {
                    notifications.forEach {
                        Text("• $it")
                    }
                }
            }
        )
    }
}

@Composable
fun NotificationBar(text: String, onBellClick: () -> Unit, onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightAqua)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = DeepPurple,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Row {
            IconButton(onClick = onBellClick) {
                Icon(Icons.Default.Notifications, contentDescription = "Bell", tint = DeepPurple)
            }
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = DeepPurple)
            }
        }
    }
}

@Composable
fun JoinBrigadeCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.2f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.BottomStart
            ) {

            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Users",
                        tint = TurquoiseBlue,
                        modifier = Modifier
                            .size(20.dp)
                            .background(TurquoiseBlue.copy(alpha = 0.1f), CircleShape)
                            .padding(4.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Join the Brigade", color = DeepPurple)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Become part of the student safety team and help keep our campus secure.",
                    color = DeepPurple.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun LearnOnYourOwnSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "BookOpen",
                tint = MintGreen,
                modifier = Modifier
                    .size(20.dp)
                    .background(MintGreen.copy(alpha = 0.1f), CircleShape)
                    .padding(4.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Learn on Your Own", color = DeepPurple)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Watch training videos and safety guides at your own pace.",
            color = DeepPurple.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.height(16.dp))


        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .fillMaxWidth()
        ) {
            videos.forEach { video ->
                VideoCardItem(video)
                Spacer(Modifier.width(16.dp))
            }
        }
    }
}

@Composable
fun VideoCardItem(video: VideoCard) {
    Card(
        modifier = Modifier
            .width(208.dp)
            .height(180.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(LightAqua),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(video.title, color = DeepPurple, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(video.duration, style = MaterialTheme.typography.bodySmall)
        }
    }
}
