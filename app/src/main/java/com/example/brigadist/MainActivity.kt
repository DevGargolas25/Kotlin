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
import com.example.brigadist.screens.DetailChat
import com.example.brigadist.ui.chat.ChatScreen
import com.example.brigadist.ui.components.BrBottomBar
import com.example.brigadist.ui.components.Destination
import com.example.brigadist.ui.home.HomeRoute
import com.example.brigadist.ui.theme.BrigadistTheme

import com.example.brigadist.ui.map.MapScreen
import com.example.brigadist.ui.profile.ProfileScreen

import com.example.brigadist.ui.videos.VideoDetailScreen
import com.example.brigadist.ui.videos.VideosRoute
import com.example.brigadist.ui.videos.model.VideoUi


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BrigadistApp() }
    }
}

@Composable
fun BrigadistApp() {
    BrigadistTheme {
        var selected by rememberSaveable { mutableStateOf(Destination.Home) }
        var selectedVideo by remember { mutableStateOf<VideoUi?>(null) }
        var showChatDetail by rememberSaveable { mutableStateOf(false) }
        var showProfile by rememberSaveable { mutableStateOf(false) }
        Scaffold(
            bottomBar = {
                BrBottomBar(
                    selected = selected,
                    onSelect = { dest ->
                        selected = dest
                        // reset inner states when switching tabs
                        if (dest == Destination.Home) showProfile = false
                        if (dest == Destination.Chat) showChatDetail = false
                    },
                    onSosClick = { /* TODO: trigger your SOS flow or confirmation */ }
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
                    Destination.Home -> {
                        if (!showProfile) {
                            HomeRoute(
                                onOpenProfile = { showProfile = true }    // <<< navigate to Profile
                            )
                        } else {
                            ProfileScreen()                                // <<< show Profile
                        }
                    }
                    Destination.Chat -> {
                        if (!showChatDetail) {
                            ChatScreen(
                                onOpenConversation = { showChatDetail = true } // <-- go to detail
                            )
                        } else {
                            DetailChat(
                                onBack = { showChatDetail = false }            // <-- back to list
                            )
                        }
                    }

                    Destination.Map    -> MapScreen()



                    Destination.Videos -> {
                        if (selectedVideo == null) {
                            VideosRoute(onVideoClick = { video -> selectedVideo = video })
                        } else {
                            VideoDetailScreen(video = selectedVideo!!, onBack = { selectedVideo = null })
                        }
                    }


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