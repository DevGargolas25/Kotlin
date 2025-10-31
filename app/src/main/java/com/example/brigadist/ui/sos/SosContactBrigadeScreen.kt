package com.example.brigadist.ui.sos

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import com.example.brigadist.data.EmergencyRepository
import com.example.brigadist.data.PendingEmergencyStore
import com.example.brigadist.ui.sos.components.EmergencyType
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    var ChatUsed by remember { mutableStateOf(false) }
    var lastEmergencyKey by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val emergencyRepository = remember { EmergencyRepository(context) }
    val pendingEmergencyStore = remember { PendingEmergencyStore(context) }

    // Sender lambda - tracks the push key so we can update ChatUsed later
    val sendMedicalEmergency: () -> Unit = {
        EmergencyActions.createAndSaveEmergency(
            context = context,
            emergencyType = EmergencyType.MEDICAL,
            emergencyRepository = emergencyRepository,
            orquestador = orquestador,
            pendingEmergencyStore = pendingEmergencyStore,
            chatUsed = ChatUsed,
            onSuccess = { key -> lastEmergencyKey = key },
            onError = { },
            onOffline = { }
        )
    }

    // Send medical alert immediately when screen appears
    LaunchedEffect(Unit) {
        sendMedicalEmergency()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {},
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

                actions = {
                    IconButton(onClick = {

                        val phoneNumber = "tel:1234567890"
                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse(phoneNumber)
                        }
                        context.startActivity(dialIntent)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Call Brigade" // For accessibility
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    titleContentColor = MaterialTheme.colorScheme.onError,
                    navigationIconContentColor = MaterialTheme.colorScheme.onError,
                    actionIconContentColor = MaterialTheme.colorScheme.onError // Make the phone icon white
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
                            onClick = {
                                selectedTab = tab
                                if (tab == ContactBrigadeTab.Assistant) {
                                    ChatUsed = true
                                    lastEmergencyKey?.let { key ->
                                        emergencyRepository.updateChatUsed(key)
                                    }
                                }
                            },
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
