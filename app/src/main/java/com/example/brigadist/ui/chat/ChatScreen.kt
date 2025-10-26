package com.example.brigadist.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.brigadist.BuildConfig
import com.example.brigadist.R
import com.example.brigadist.analytics.AnalyticsHelper
import com.example.brigadist.utils.NetworkConnectivityHelper
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.*

private data class UiMessage(
    val role: String,
    val content: String,
    val isOfflineFallback: Boolean = false
)

private const val SYSTEM_PROMPT = """
You are a calm, concise campus emergency assistant. Always prioritize immediate safety.
Provide short, numbered steps for situations like fire, earthquake, bleeding, choking, burns, fractures, and evacuations.
If danger is life-threatening, first instruct the user to call the local emergency number.
For untrained users and an unresponsive adult not breathing normally, advise hands-only CPR.
Give immediate steps before asking one focused follow-up question if needed. English only.
"""

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
    
    val messages = rememberSaveable { mutableStateListOf(UiMessage("system", SYSTEM_PROMPT)) }
    val listState = rememberLazyListState()
    
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
                    if (sending) {
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
                    items(messages.drop(1).asReversed(), key = { "${it.role}_${it.content}" }) { msg ->
                        MessageBubble(msg)
                        Spacer(Modifier.height(8.dp))
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
                        input = ""
                        sending = true

                        scope.launch(Dispatchers.IO) {
                            try {
                                // Check network connectivity before making API call
                                val isOnline = NetworkConnectivityHelper.isNetworkAvailable(context)

                                if (!isOnline) {
                                    // Use offline fallback immediately
                                    val fallbackMessage = context.getString(R.string.offline_fallback_message)
                                    val locale = Locale.getDefault().language

                                    // Track offline fallback usage
                                    AnalyticsHelper.trackOfflineFallbackUsed(
                                        reason = "no_internet",
                                        appVersion = BuildConfig.VERSION_NAME,
                                        locale = locale
                                    )

                                    withContext(Dispatchers.Main) {
                                        messages.add(UiMessage("assistant", fallbackMessage, isOfflineFallback = true))
                                        sending = false
                                    }
                                    return@launch
                                }

                                // Network available, proceed with Groq API call
                                val reply = callGroq(messages.filter { it.role != "system" })
                                withContext(Dispatchers.Main) {
                                    messages.add(UiMessage("assistant", reply))
                                    sending = false
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    // Check if exception is network-related
                                    val isNetworkError = e is SocketTimeoutException ||
                                                        e is UnknownHostException ||
                                                        e is java.net.ConnectException ||
                                                        e.message?.contains("timeout", ignoreCase = true) == true ||
                                                        e.message?.contains("network", ignoreCase = true) == true

                                    if (isNetworkError) {
                                        // Use offline fallback for network errors
                                        val fallbackMessage = context.getString(R.string.offline_fallback_message)
                                        val locale = Locale.getDefault().language

                                        AnalyticsHelper.trackOfflineFallbackUsed(
                                            reason = "network_error",
                                            appVersion = BuildConfig.VERSION_NAME,
                                            locale = locale
                                        )

                                        messages.add(UiMessage("assistant", fallbackMessage, isOfflineFallback = true))
                                    } else {
                                        // Other errors, show generic error without exposing stack trace
                                        messages.add(UiMessage("assistant", "I'm having trouble right now. If this is an emergency, call your local emergency number immediately."))
                                        showError = true
                                    }
                                    sending = false
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
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
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Show sender label and offline tag if applicable
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isUser) "You" else "Chat",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isUser) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )

                    // Show offline tag for fallback messages
                    if (message.isOfflineFallback) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.offline_fallback_tag),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
            }
        }
    }
}