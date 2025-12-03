package com.example.brigadist.ui.notifications.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.brigadist.ui.notifications.model.Notification

/**
 * A simple popup card to show a preview of notifications.
 */
@Composable
fun NotificationPopup(
    notifications: List<Notification>,
    onDismiss: () -> Unit,
    onSeeAllClick: () -> Unit,
    onNotificationClick: (Notification) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.width(320.dp),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Notificaciones", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            if (notifications.isEmpty()) {
                Text("No hay notificaciones nuevas", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 16.dp))
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    // Show a preview, e.g., the first 5 notifications
                    items(notifications.take(5)) { n ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNotificationClick(n) }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                n.title, 
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (n.read) FontWeight.Normal else FontWeight.Bold
                            )
                            Text(
                                n.message, 
                                style = MaterialTheme.typography.bodySmall, 
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                            Spacer(Modifier.height(6.dp))
                            Divider()
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Cerrar") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onSeeAllClick) { Text("Ver todas") }
            }
        }
    }
}