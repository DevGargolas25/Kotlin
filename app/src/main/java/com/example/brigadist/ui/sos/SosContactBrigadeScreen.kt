package com.example.brigadist.ui.sos

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.brigadist.Orquestador
import com.example.brigadist.R
import com.example.brigadist.ui.chat.ChatScreen
import com.example.brigadist.ui.sos.components.ContactBrigadeLocationTab
import com.example.brigadist.ui.sos.components.ContactBrigadeMedicalTab

enum class ContactBrigadeTab {
    Location, Assistant, Medical
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SosContactBrigadeScreen(
    orquestador: Orquestador,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(ContactBrigadeTab.Location) }
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Contact Brigade",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.semantics {
                            contentDescription = "Back to emergency options"
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    titleContentColor = MaterialTheme.colorScheme.onError,
                    navigationIconContentColor = MaterialTheme.colorScheme.onError
                )
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    modifier = Modifier.semantics {
                        contentDescription = "Contact Brigade tabs"
                    }
                ) {
                    ContactBrigadeTab.values().forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            modifier = Modifier.semantics {
                                contentDescription = when (tab) {
                                    ContactBrigadeTab.Location -> "Location tab - View emergency location and brigade member"
                                    ContactBrigadeTab.Assistant -> "Assistant tab - Chat with emergency AI assistant"
                                    ContactBrigadeTab.Medical -> "Medical tab - View medical information"
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = when (tab) {
                                        ContactBrigadeTab.Location -> Icons.Default.LocationOn
                                        ContactBrigadeTab.Assistant -> Icons.Default.Person
                                        ContactBrigadeTab.Medical -> Icons.Default.Favorite
                                    },
                                    contentDescription = null
                                )
                            },
                            text = {
                                Text(
                                    text = when (tab) {
                                        ContactBrigadeTab.Location -> stringResource(R.string.contact_brigade_tab_location)
                                        ContactBrigadeTab.Assistant -> stringResource(R.string.contact_brigade_tab_assistant)
                                        ContactBrigadeTab.Medical -> stringResource(R.string.contact_brigade_tab_medical)
                                    }
                                )
                            }
                        )
                    }
                }
                
                // Tab Content
                Box(modifier = Modifier.fillMaxSize()) {
                    when (selectedTab) {
                        ContactBrigadeTab.Location -> {
                            ContactBrigadeLocationTab(
                                orquestador = orquestador,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        ContactBrigadeTab.Assistant -> {
                            ChatScreen()
                        }
                        ContactBrigadeTab.Medical -> {
                            ContactBrigadeMedicalTab(
                                orquestador = orquestador,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
