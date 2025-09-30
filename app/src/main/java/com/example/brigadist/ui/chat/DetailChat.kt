package com.example.brigadist.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A simple data class representing a single chat message.
 *
 * @param sender Human‑readable sender name. For the current user this may be "Me".
 * @param text   The message body.
 * @param time   A short timestamp such as "9:35 AM".
 * @param isUser Whether or not the message belongs to the current user. Used to
 *               customise alignment and colouring.
 */
data class ChatMessage(
    val sender: String,
    val text: String,
    val time: String,
    val isUser: Boolean
)

/**
 * A composable that replicates the brigade team chat screen illustrated in the provided
 * screenshot. It includes a coloured app bar, a scrollable list of messages with
 * distinctly styled bubbles, an input bar and a bottom navigation bar with a prominent SOS button.
 *
 * @param modifier A modifier for styling the top‑level container.
 * @param onNavigateHome Callback invoked when the home navigation item is tapped.
 * @param onNavigateChat Callback invoked when the chat navigation item is tapped.
 * @param onNavigateMap Callback invoked when the map navigation item is tapped.
 * @param onNavigateVideos Callback invoked when the videos navigation item is tapped.
 * @param onSOS Callback invoked when the SOS button is tapped. Triggered after confirmation.
 */
@Composable
fun DetailChat(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onNavigateHome: () -> Unit = {},
    onNavigateChat: () -> Unit = {},
    onNavigateMap: () -> Unit = {},
    onNavigateVideos: () -> Unit = {},
    onSOS: () -> Unit = {},
) {
    // Hard‑coded sample conversation mirroring the screenshot. In a real application
    // this would be supplied by your view model or repository layer.
    val messages = listOf(
        ChatMessage(
            sender = "Captain Rodriguez",
            text = "Good morning everyone! Reminder about tonight's meeting at 7 PM in room 203.",
            time = "9:30 AM",
            isUser = false
        ),
        ChatMessage(
            sender = "Me",
            text = "I'll be there. Should I bring the safety equipment reports?",
            time = "9:35 AM",
            isUser = true
        ),
        ChatMessage(
            sender = "Captain Rodriguez",
            text = "Yes please! Also, we'll be discussing the new emergency protocols.",
            time = "9:40 AM",
            isUser = false
        ),
        ChatMessage(
            sender = "Maria S.",
            text = "I can help with the presentation setup if needed.",
            time = "9:42 AM",
            isUser = false
        ),
        ChatMessage(
            sender = "Captain Rodriguez",
            text = "Perfect! Meeting tonight at 7 PM in room 203. Please confirm your attendance.",
            time = "9:45 AM",
            isUser = false
        )
    )

    // Scaffold manages the positioning of top and bottom bars relative to the main content.
    Scaffold(
        topBar = { ChatTopBar(onBack = onBack) },
        bottomBar = {
            Column {
                // Input bar at the very bottom of the message list
                MessageInputBar()
                // Small spacer to separate the input bar from the navigation
                Spacer(modifier = Modifier.height(4.dp))

            }
        },
        modifier = modifier
    ) { innerPadding ->
        // Main scrollable content for the list of messages.
        LazyColumn(
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp, start = 12.dp, end = 12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(messages) { message ->
                MessageRow(message = message)
            }
        }
    }
}

/**
 * Top bar containing a back arrow, a group icon and the group title and subtitle. The
 * colours and arrangement mirror the screenshot. The bar remains fixed at the top of
 * the screen.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ChatTopBar(onBack: () -> Unit = {}) {
    // Tint values derived from the supplied design. Changing these here propagates
    // throughout the bar for easier theming.
    val containerColour = MaterialTheme.colorScheme.primary
    val textPrimary = MaterialTheme.colorScheme.onPrimary
    val textSecondary = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)

    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = textPrimary
                )
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Group icon rendered as a generic person icon
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = textPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Brigade Team",
                        color = textPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "12 members",
                        color = textSecondary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColour,
            navigationIconContentColor = textPrimary,
            titleContentColor = textPrimary
        )
    )
}

/**
 * Renders a single row in the conversation list. Non‑user messages are aligned to the
 * start of the row and include a coloured name label, while user messages are
 * right‑aligned and omit the name. Avatars appear on the side opposite the alignment.
 */
@Composable
fun MessageRow(message: ChatMessage) {
    val bubbleShape = RoundedCornerShape(16.dp)
    // Define base palette derived from the reference implementation.
    val userBubbleColour = MaterialTheme.colorScheme.secondary
    val otherBubbleColour = MaterialTheme.colorScheme.primaryContainer
    val senderColour = MaterialTheme.colorScheme.secondary
    val bodyColour = MaterialTheme.colorScheme.onSurface
    val timeColour = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    Row(
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        // Avatar on the left for other messages
        if (!message.isUser) {
            AvatarCircle(colour = otherBubbleColour)
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = if (message.isUser) userBubbleColour else otherBubbleColour,
                        shape = bubbleShape
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.widthIn(max = 260.dp)) {
                    // Only show sender name for messages not from the current user
                    if (!message.isUser) {
                        Text(
                            text = message.sender,
                            color = senderColour,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    Text(
                        text = message.text,
                        color = bodyColour,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            // Timestamp below the bubble
            Text(
                text = message.time,
                color = timeColour,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Avatar on the right for user messages
        if (message.isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            AvatarCircle(colour = userBubbleColour)
        }
    }
}

/**
 * Simple circular avatar using the provided colour. In a real application you might
 * substitute this with a profile picture or generated initials.
 */
@Composable
fun AvatarCircle(colour: Color) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(colour.copy(alpha = 0.3f), shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = colour,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Input bar displayed above the navigation bar. It contains a text field with a hint
 * and a send button. The send button currently does not perform any actions.
 */
@Composable
fun MessageInputBar() {
    var inputValue by remember { mutableStateOf("") }
    val borderColour = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f) // semi‑transparent border for the text field
    val backgroundColour = MaterialTheme.colorScheme.surface
    val hintColour = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    val sendColour = MaterialTheme.colorScheme.primary

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColour)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        TextField(
            value = inputValue,
            onValueChange = { inputValue = it },
            placeholder = { Text(text = "Type a message...", color = hintColour) },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedIndicatorColor = borderColour,
                focusedIndicatorColor = borderColour,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = sendColour
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = { /* TODO: send message */ }) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send",
                tint = sendColour,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

/**
 * Bottom navigation bar with five items: Home, Chat, SOS (centrally raised), Map and Videos.
 * Selected items are tinted and given a subtle background, while the central SOS button
 * uses a red palette and larger size to draw attention. Colour values mirror the
 * React sample from the provided zip file.
 */

/**
 * Helper composable for standard navigation buttons in the bottom navigation bar. Each
 * button shows an icon and a label stacked vertically. When selected, the icon and
 * label are tinted and the button has a lightly coloured background. When
 * unselected, the icon and label are muted and only change colour on hover.
 */
@Composable
fun NavigationButton(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    selectedColour: Color,
    unselectedColour: Color,
    onClick: () -> Unit
) {
    val backgroundColour = if (selected) selectedColour.copy(alpha = 0.1f) else Color.Transparent
    val contentColour = if (selected) selectedColour else unselectedColour
    Column(
        modifier = Modifier
            .background(backgroundColour, shape = RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColour,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            color = contentColour,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}

/**
 * Centrally placed SOS button. It is larger than the other navigation items and uses
 * a red palette. A small label appears beneath the icon. Typically you would wrap
 * this in a confirmation dialog before actually triggering an emergency procedure.
 */
@Composable
fun SOSButton(colour: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .size(64.dp)
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(colour, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "SOS",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = "SOS",
            color = colour,
            style = MaterialTheme.typography.labelSmall
        )
    }
}