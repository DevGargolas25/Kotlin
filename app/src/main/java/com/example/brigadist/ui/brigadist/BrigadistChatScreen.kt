package com.example.brigadist.ui.brigadist

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.brigadist.BuildConfig
import com.example.brigadist.data.AssistantLikesRepository
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

private data class UiMessage(val role: String, val content: String, val isPending: Boolean = false)

// System prompt for Brigadist - focused on attending and guiding people through emergencies
private const val BRIGADIST_SYSTEM_PROMPT = """
You are an emergency response assistant for campus brigadists (emergency responders). Your role is to help brigadists guide and assist people during emergencies.

Provide clear, actionable instructions for:
• How to assess emergency situations
• How to guide people through fire evacuations
• How to assist during earthquakes
• First aid procedures and life-saving techniques
• How to coordinate with emergency services
• How to manage crowds during evacuations
• How to provide calm, clear instructions to people in distress

Always prioritize:
1. Ensuring your own safety first
2. Calling emergency services (123 in Colombia)
3. Providing clear, step-by-step guidance
4. Keeping people calm and following proper procedures

Give immediate, numbered steps for emergency response. Be concise and practical. English only.
"""

// Emergency response fallback message for brigadists
private const val BRIGADIST_EMERGENCY_FALLBACK_MESSAGE = """Emergency Response Guide for Brigadists

FIRE RESPONSE — How to guide people
• Ensure your own safety first - don't enter unsafe areas
• Call 123 immediately and provide location details
• Guide people to use stairs, never elevators
• Direct people to stay low to avoid smoke
• Help people who are having difficulty evacuating
• Once outside, guide people to safe assembly points
• Account for people if possible
• Keep people away from the building

EARTHQUAKE RESPONSE — How to assist
• Ensure your own safety - DROP, COVER, HOLD ON
• After shaking stops, check for injuries
• Guide people to evacuate if building is damaged
• Help people with mobility issues
• Direct people to open areas away from buildings
• Be prepared for aftershocks
• Coordinate with emergency services when they arrive

FIRST AID BASICS
• Check for responsiveness
• Call 123 for serious injuries
• Control bleeding with direct pressure
• Keep injured person calm and still
• Don't move people with potential spinal injuries
• Provide CPR if trained and person is unresponsive

Colombia emergency numbers
• 123 — National unified emergency line (recommended)
• 119 — Firefighters (direct)
• 125 — Ambulances / Secretariat of Health
• 132 — Colombian Red Cross
• 112 — National Police (alternate)

When you regain connection, I'll resume normal assistance."""

private fun isNetworkError(exception: Exception): Boolean {
    return exception is IOException ||
            exception is SocketTimeoutException ||
            exception is UnknownHostException ||
            exception.message?.contains("network", ignoreCase = true) == true ||
            exception.message?.contains("host", ignoreCase = true) == true ||
            exception.message?.contains("timeout", ignoreCase = true) == true
}

private fun callGroqBrigadist(history: List<UiMessage>): String {
    val apiKey = BuildConfig.GROQ_API_KEY
    require(apiKey.isNotBlank()) { "Missing GROQ_API_KEY" }

    val client = OkHttpClient()
    val json = JSONObject().apply {
        put("model", "llama-3.1-8b-instant")
        val msgs = JSONArray()
        // System first
        msgs.put(JSONObject().put("role","system").put("content", BRIGADIST_SYSTEM_PROMPT.trim()))
        // Then history
        history.forEach { m ->
            msgs.put(JSONObject().put("role", m.role).put("content", m.content))
        }
        put("messages", msgs)
        put("max_tokens", 512)
        put("temperature", 0.2)
    }.toString()

    val req = Request.Builder()
        .url("https://api.groq.com/openai/v1/chat/completions")
        .addHeader("Authorization", "Bearer $apiKey")
        .addHeader("Content-Type", "application/json")
        .post(json.toRequestBody("application/json; charset=utf-8".toMediaType()))
        .build()

    client.newCall(req).execute().use { resp ->
        if (!resp.isSuccessful) error("Groq ${resp.code}: ${resp.body?.string()}")
        val body = resp.body?.string().orEmpty()
        val jo = JSONObject(body)
        return jo.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
            .trim()
    }
}

@Composable
fun BrigadistChatScreen() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var input by rememberSaveable { mutableStateOf("") }
    var sending by rememberSaveable { mutableStateOf(false) }
    var showError by rememberSaveable { mutableStateOf(false) }
    var isOffline by rememberSaveable { mutableStateOf(false) }
    var showOfflineBanner by rememberSaveable { mutableStateOf(false) }
    
    val messages = rememberSaveable { mutableStateListOf(UiMessage("system", BRIGADIST_SYSTEM_PROMPT)) }
    // Per-message vote state for this session: true=like, false=dislike
    val votes = rememberSaveable(
        saver = mapSaver(
            save = { it.toMap() },
            restore = { restored -> mutableStateMapOf<String, Boolean>().apply { putAll(restored as Map<String, Boolean>) } }
        )
    ) { mutableStateMapOf<String, Boolean>() }
    val listState = rememberLazyListState()
    
    // Track last pending message index for retry
    var lastPendingMessageIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    
    // Connectivity monitoring
    val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
    
    LaunchedEffect(Unit) {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isOffline = false
            }
            
            override fun onLost(network: Network) {
                isOffline = true
            }
            
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                isOffline = !hasInternet
            }
        }
        
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, callback)
        
        // Check initial connectivity
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }
        val hasInternet = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        isOffline = !hasInternet
    }
    
    // Auto-scroll to bottom when new messages are added
    LaunchedEffect(messages.size) {
        if (messages.size > 1) { // Skip system message
            listState.animateScrollToItem(0)
        }
    }
    
    // Show error snackbar
    if (showError) {
        LaunchedEffect(Unit) {
            delay(3000)
            showError = false
        }
    }
    
    // Offline banner visibility
    if (showOfflineBanner && !isOffline) {
        LaunchedEffect(Unit) {
            showOfflineBanner = false
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Message list
        Box(modifier = Modifier.weight(1f)) {
            if (messages.size <= 1) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ask me about emergency response:\n• Fire evacuation procedures\n• Earthquake response\n• First aid techniques\n• Crowd management\n• Emergency coordination",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    reverseLayout = true,
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    // Show typing indicator if sending
                    if (sending && !isOffline) {
                        item {
                            Row {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        text = "Chat is typing...",
                                        modifier = Modifier.padding(12.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                    
                    // Messages (skip system message)
                    items(messages.drop(1).asReversed(), key = { "${it.role}_${it.content}_${it.isPending}" }) { msg ->
                        val vote = votes[msg.content]
                        MessageBubble(message = msg, vote = vote) { liked ->
                            if (vote == null) {
                                if (liked) AssistantLikesRepository.incrementLike() else AssistantLikesRepository.incrementDislike()
                                votes[msg.content] = liked
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        // Offline banner with actions
        if (showOfflineBanner) {
            Surface(
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "No internet connection.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Row {
                        TextButton(
                            onClick = {
                                // Retry logic
                                lastPendingMessageIndex?.let { index ->
                                    if (!isOffline && index in messages.indices) {
                                        val pendingMessage = messages[index]
                                        messages[index] = pendingMessage.copy(isPending = false)
                                        sending = true
                                        
                                        scope.launch(Dispatchers.IO) {
                                            try {
                                                val reply = callGroqBrigadist(messages.filter { it.role != "system" })
                                                withContext(Dispatchers.Main) {
                                                    messages.add(UiMessage("assistant", reply))
                                                    sending = false
                                                    showOfflineBanner = false
                                                    lastPendingMessageIndex = null
                                                }
                                            } catch (e: Exception) {
                                                withContext(Dispatchers.Main) {
                                                    messages[index] = pendingMessage
                                                    sending = false
                                                    if (!isNetworkError(e)) {
                                                        showError = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            enabled = !isOffline
                        ) {
                            Text("Retry", style = MaterialTheme.typography.labelLarge)
                        }
                        
                        Spacer(Modifier.width(8.dp))
                        
                        TextButton(
                            onClick = {
                                // Insert emergency response guide
                                messages.add(UiMessage("assistant", BRIGADIST_EMERGENCY_FALLBACK_MESSAGE))
                            }
                        ) {
                            Text("Emergency guide", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        // Input area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("Ask about emergency response procedures...") },
                modifier = Modifier.weight(1f),
                enabled = !sending,
                maxLines = 1
            )
            
            Spacer(Modifier.width(8.dp))
            
            FloatingActionButton(
                onClick = {
                    if (input.isNotBlank() && !sending) {
                        val userMessage = UiMessage("user", input.trim())
                        messages.add(userMessage)
                        val currentInput = input
                        val messageIndex = messages.size - 1
                        input = ""
                        sending = true
                        
                        scope.launch(Dispatchers.IO) {
                            try {
                                val reply = callGroqBrigadist(messages.filter { it.role != "system" })
                                withContext(Dispatchers.Main) {
                                    messages.add(UiMessage("assistant", reply))
                                    sending = false
                                    showOfflineBanner = false
                                    lastPendingMessageIndex = null
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    if (isNetworkError(e)) {
                                        // Mark message as pending and show banner
                                        messages[messageIndex] = userMessage.copy(isPending = true)
                                        lastPendingMessageIndex = messageIndex
                                        showOfflineBanner = true
                                    } else {
                                        messages.add(UiMessage("assistant", "I'm having trouble right now. If this is an emergency, call 123 immediately."))
                                        showError = true
                                    }
                                    sending = false
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.size(48.dp).alpha(if (!sending) 1f else 0.5f),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send message",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        // Error snackbar
        if (showError) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { showError = false }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text("Connection error. Please try again.")
            }
        }
    }
}

@Composable
private fun MessageBubble(message: UiMessage, vote: Boolean?, onVote: (Boolean) -> Unit) {
    val isUser = message.role == "user"
    val isPending = message.isPending
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.widthIn(max = 280.dp).alpha(if (isPending) 0.6f else 1f)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUser) "You" else "Chat",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isUser) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    if (isPending) {
                        Spacer(Modifier.width(4.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Pending",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!isUser) {
                    Spacer(Modifier.height(6.dp))
                    Row {
                        val canVote = vote == null && !isPending
                        val likeTint = if (vote == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        val dislikeTint = if (vote == false) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        IconButton(onClick = { if (canVote) onVote(true) }, enabled = canVote) {
                            Icon(imageVector = Icons.Filled.ThumbUp, contentDescription = "Like", tint = likeTint)
                        }
                        Spacer(Modifier.width(4.dp))
                        IconButton(onClick = { if (canVote) onVote(false) }, enabled = canVote) {
                            Icon(imageVector = Icons.Filled.ThumbDown, contentDescription = "Dislike", tint = dislikeTint)
                        }
                    }
                }
            }
        }
    }
}

