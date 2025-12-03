package com.example.brigadist.ui.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBack: () -> Unit = {}
) {
    val vm: NotificationViewModel = viewModel()
    val list by vm.notifications.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificacionts") },
                navigationIcon = {
                    IconButton(onClick = onBack) { 
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver") 
                    }
                }
            )
        }
    ) { padding ->
        if (list.isEmpty() && vm.loading.collectAsState().value) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
            }
        } else if (list.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Text("No notifications", modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
            }
        } else {
            LazyColumn(contentPadding = padding, modifier = Modifier.fillMaxSize()) {
                items(list) { n ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { vm.markAsRead(n.id) }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            n.title, 
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (n.read) FontWeight.Normal else FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            n.message, 
                            style = MaterialTheme.typography.bodyMedium, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Divider()
                    }
                }
            }
        }
    }
}
