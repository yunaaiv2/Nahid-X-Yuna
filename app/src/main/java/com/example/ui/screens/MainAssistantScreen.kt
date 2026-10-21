package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.db.entities.ChatMessageEntity
import com.example.ui.components.YunaCharacterView
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NahidH4cksAccent
import com.example.ui.theme.NeonPink
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.YunaAssistantViewModel
import com.example.voice.VoiceState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAssistantScreen(
    viewModel: YunaAssistantViewModel,
    onNavigatePermissions: () -> Unit,
    onNavigatePhoneActions: () -> Unit,
    onNavigateSmartMemory: () -> Unit,
    onNavigateChatHistory: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateMessaging: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Microphone permission launcher
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startListening()
        }
    }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "YUNA AI",
                                    color = TextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "유나",
                                    color = NeonPink,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                // Online indicator dot
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (uiState.isOnline) SuccessGreen else Color.Gray)
                                )
                            }
                            Text(
                                text = "By Nahid H4cks",
                                color = NahidH4cksAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                actions = {
                    // Floating overlay toggle button
                    IconButton(
                        onClick = { viewModel.toggleFloatingAssistant(context) },
                        modifier = Modifier.testTag("floating_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Floating Assistant",
                            tint = if (uiState.isFloatingActive) NeonPink else TextSecondary
                        )
                    }
                    // Phone Actions
                    IconButton(
                        onClick = onNavigatePhoneActions,
                        modifier = Modifier.testTag("phone_actions_button")
                    ) {
                        Icon(imageVector = Icons.Default.Build, contentDescription = "Phone Actions", tint = ElectricBlue)
                    }
                    // Permissions
                    IconButton(
                        onClick = onNavigatePermissions,
                        modifier = Modifier.testTag("permissions_button")
                    ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = "Permissions", tint = TextPrimary)
                    }
                    // Memory
                    IconButton(
                        onClick = onNavigateSmartMemory,
                        modifier = Modifier.testTag("smart_memory_button")
                    ) {
                        Icon(imageVector = Icons.Default.Psychology, contentDescription = "Smart Memory", tint = TextPrimary)
                    }
                    // History
                    IconButton(
                        onClick = onNavigateChatHistory,
                        modifier = Modifier.testTag("chat_history_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Chat, contentDescription = "History", tint = TextPrimary)
                    }
                    // Voice Messaging Module
                    IconButton(
                        onClick = onNavigateMessaging,
                        modifier = Modifier.testTag("messaging_screen_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Voice Messaging", tint = NeonPink)
                    }
                    // Settings
                    IconButton(
                        onClick = onNavigateSettings,
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Interactive Holographic Character Component
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                YunaCharacterView(
                    voiceState = uiState.voiceState,
                    audioAmplitude = uiState.audioAmplitude,
                    personality = settings.personality,
                    reducedMotion = settings.reducedMotion
                )
            }

            // Quick Suggestion Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                val suggestions = listOf(
                    "💬 Voice Messaging",
                    "⚡ Flashlight ON",
                    "🔋 Battery Status",
                    "📱 Open YouTube",
                    "🧮 125 * 8",
                    "⏰ What time is it?",
                    "🔙 Go Back"
                )
                items(suggestions) { item ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurfaceVariant)
                            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                            .clickable {
                                if (item == "💬 Voice Messaging") {
                                    onNavigateMessaging()
                                } else {
                                    val clean = item.substringAfter(" ").trim()
                                    viewModel.sendMessage(clean)
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = item, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Action Feedback Notification (if any)
            AnimatedVisibility(
                visible = uiState.lastActionResult != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF16233B))
                        .border(1.dp, ElectricBlue.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = uiState.lastActionResult ?: "",
                        color = ElectricBlue,
                        fontSize = 11.sp
                    )
                }
            }

            // Chat Message Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(uiState.messages) { message ->
                    ChatMessageItem(
                        message = message,
                        onReplaySpeak = { viewModel.speak(message.content) }
                    )
                }
                if (uiState.isProcessing) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = NeonPink,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Yuna is thinking...",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Bottom Input Dock
            Surface(
                color = DarkSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Emergency Stop / Stop Speaking Button
                    if (uiState.voiceState == VoiceState.SPEAKING || uiState.voiceState == VoiceState.LISTENING || uiState.isProcessing) {
                        IconButton(
                            onClick = { viewModel.emergencyStop() },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(ErrorRed.copy(alpha = 0.2f))
                                .border(1.dp, ErrorRed, CircleShape)
                                .testTag("stop_action_button")
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop", tint = ErrorRed)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    // Text Input Field
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask or command Yuna...", color = TextSecondary, fontSize = 13.sp) },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = DarkSurfaceVariant,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = NeonPink
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("message_input_field")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    if (inputText.isNotBlank()) {
                        // Send Button
                        IconButton(
                            onClick = {
                                val text = inputText.trim()
                                inputText = ""
                                viewModel.sendMessage(text)
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(ElectricBlue)
                                .testTag("send_message_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color.Black
                            )
                        }
                    } else {
                        // Microphone Button
                        val isListening = uiState.voiceState == VoiceState.LISTENING
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isListening) Brush.radialGradient(listOf(ElectricBlue, NeonPink))
                                    else Brush.radialGradient(listOf(NeonPink, Color(0xFF6B0E3C)))
                                )
                                .border(
                                    2.dp,
                                    if (isListening) ElectricBlue else NeonPink,
                                    CircleShape
                                )
                                .clickable {
                                    if (isListening) {
                                        viewModel.stopListening()
                                    } else {
                                        val hasPermission = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.RECORD_AUDIO
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (hasPermission) {
                                            viewModel.startListening()
                                        } else {
                                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                                }
                                .testTag("mic_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = if (isListening) "Listening" else "Speak",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessageEntity,
    onReplaySpeak: () -> Unit
) {
    val isUser = message.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp
                    )
                )
                .background(
                    if (isUser) Color(0xFF261A42)
                    else Color(0xFF141724)
                )
                .border(
                    1.dp,
                    if (isUser) Color(0x669D4EDD)
                    else NeonPink.copy(alpha = 0.35f),
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp
                    )
                )
                .padding(12.dp)
        ) {
            Column {
                if (!isUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "YUNA AI",
                            color = NeonPink,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = onReplaySpeak,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Speak",
                                tint = ElectricBlue,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = message.content,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                if (message.actionTag != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ElectricBlue.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "⚡ Action: ${message.actionTag}",
                            color = ElectricBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
