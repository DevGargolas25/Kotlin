package com.example.brigadist.ui.chat

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

private const val SYSTEM_PROMPT = """
You are a calm, concise campus emergency assistant. Always prioritize immediate safety.
Provide short, numbered steps for situations like fire, earthquake, bleeding, choking, burns, fractures, and evacuations.
If danger is life-threatening, first instruct the user to call the local emergency number.
For untrained users and an unresponsive adult not breathing normally, advise hands-only CPR.
Give immediate steps before asking one focused follow-up question if needed. English only.
"""

// Emergency fallback message content
private const val EMERGENCY_FALLBACK_MESSAGE = """If you're in immediate danger, call 123 now.

FIRE — What to do
• Get low to avoid smoke; cover mouth and nose.
• Use stairs, never elevators.
• If clothes catch fire: STOP, DROP & ROLL.
• Close doors behind you to slow fire spread.
• Once outside, stay out and go to a safe point.

EARTHQUAKE — What to do
• DROP, COVER, and HOLD ON under sturdy furniture or next to an interior wall.
• Stay away from windows and heavy objects.
• If you're outside, move to an open area away from facades and power lines.
• Expect aftershocks; evacuate calmly when shaking stops and routes are clear.

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

private fun callGroq(history: List<UiMessage>): String {
    val apiKey = BuildConfig.GROQ_API_KEY
    require(apiKey.isNotBlank()) { "Missing GROQ_API_KEY" }

    val client = OkHttpClient()
    val json = JSONObject().apply {
        put("model", "llama-3.1-8b-instant")
        val msgs = JSONArray()
        // System first
        msgs.put(JSONObject().put("role","system").put("content", SYSTEM_PROMPT.trim()))
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
fun ChatScreen() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var input by rememberSaveable { mutableStateOf("") }
    var sending by rememberSaveable { mutableStateOf(false) }
    var showError by rememberSaveable { mutableStateOf(false) }
    var isOffline by rememberSaveable { mutableStateOf(false) }
    var showOfflineBanner by rememberSaveable { mutableStateOf(false) }
    
    val messages = rememberSaveable { mutableStateListOf(UiMessage("system", SYSTEM_PROMPT)) }
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
                        text = "Ask me about emergency procedures:\n• Fire safety\n• Earthquake response\n• First aid\n• Evacuation steps",
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
                        MessageBubble(msg)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        // Offline banner with actions
        if (showOfflineBanner) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
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
                        color = MaterialTheme.colorScheme.onErrorContainer,
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
                                                val reply = callGroq(messages.filter { it.role != "system" })
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
                                // Insert emergency tips
                                messages.add(UiMessage("assistant", EMERGENCY_FALLBACK_MESSAGE))
                            }
                        ) {
                            Text("Emergency tips", style = MaterialTheme.typography.labelLarge)
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
                placeholder = { Text("Ask about emergency procedures...") },
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
                                val reply = callGroq(messages.filter { it.role != "system" })
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
                                        messages.add(UiMessage("assistant", "I'm having trouble right now. If this is an emergency, call your local emergency number immediately."))
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
private fun MessageBubble(message: UiMessage) {
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
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Pending",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
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

                if (!isUser && !isPending) {
                    Spacer(Modifier.height(6.dp))
                    Row {
                        IconButton(onClick = { AssistantLikesRepository.incrementLike() }) {
                            Icon(imageVector = Icons.Filled.ThumbUp, contentDescription = "Like")
                        }
                        Spacer(Modifier.width(4.dp))
                        IconButton(onClick = { AssistantLikesRepository.incrementDislike() }) {
                            Icon(imageVector = Icons.Filled.ThumbDown, contentDescription = "Dislike")
                        }
                    }
                }
            }
        }
    }
}