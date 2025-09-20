package com.example.brigadist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.brigadist.ui.components.BrBottomBar
import com.example.brigadist.ui.components.BrSosFab
import com.example.brigadist.ui.components.Destination
import com.example.brigadist.ui.home.HomeRoute
import com.example.brigadist.ui.theme.BrigadistTheme
import com.example.brigadist.ui.map.MapScreen

class MainActivity : ComponentActivity() {        // use this name in your manifest
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BrigadistApp() }
    }
}

@Composable
fun BrigadistApp() {
    BrigadistTheme {
        var selected by rememberSaveable { mutableStateOf(Destination.Home) }

        Scaffold(
            floatingActionButton = { BrSosFab { /* TODO SOS action */ } },
            floatingActionButtonPosition = FabPosition.Center,
            bottomBar = {
                BrBottomBar(
                    selected = selected,
                    onSelect = { selected = it }
                )
            }
        ) { inner ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner),
                color = MaterialTheme.colorScheme.background
            ) {
                when (selected) {
                    Destination.Home   -> HomeRoute()
                    Destination.Chat   -> Placeholder("Chat (coming soon)")
                    Destination.Map    -> MapScreen()
                    Destination.Videos -> Placeholder("Videos (coming soon)")
                }
            }
        }
    }
}

@Composable
private fun Placeholder(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}
