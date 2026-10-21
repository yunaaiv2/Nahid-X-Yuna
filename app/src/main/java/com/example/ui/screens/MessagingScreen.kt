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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.accessibility.YunaAccessibilityService
import com.example.messaging.model.MessagingExecutionStage
import com.example.messaging.model.MessagingPlatform
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
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.MessagingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingScreen(
    messagingViewModel: MessagingViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val settings by messagingViewModel.settings.collectAsState()
    val pendingCommand by messagingViewModel.pendingCommand.collectAsState()
    val isConfirmPreviewVisible by messagingViewModel.isConfirmPreviewVisible.collectAsState()
    val isAmbiguityVisible by messagingViewModel.isAmbiguityVisible.collectAsState()
    val lastResult by messagingViewModel.lastResult.collectAsState()
    val actionLogs by messagingViewModel.actionLogs.collectAsState()
    val isDispatching by messagingViewModel.isDispatching.collectAsState()

    var testInputText by remember { mutableStateOf("") }
    var clarifiedRecipientInput by remember { mutableStateOf("") }

    // SMS permission request launcher for direct SMS
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        messagingViewModel.toggleDirectSms(granted)
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Voice Messaging Assistant",
                                color = TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "WhatsApp • Telegram • SMS • Messenger",
                            color = ElectricBlue,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    // Global Emergency Stop Button
                    IconButton(
                        onClick = { messagingViewModel.emergencyStop() },
                        modifier = Modifier.testTag("emergency_stop_messaging_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.StopCircle,
                            contentDescription = "Emergency Stop",
                            tint = ErrorRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Master Enable/Disable & Status Card
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (settings.isMasterEnabled) NeonPink.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                tint = if (settings.isMasterEnabled) NeonPink else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Voice Messaging System",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = if (settings.isMasterEnabled) "Active • Listening for message requests" else "Disabled by master toggle",
                                color = if (settings.isMasterEnabled) SuccessGreen else TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = settings.isMasterEnabled,
                            onCheckedChange = { messagingViewModel.toggleMaster(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeonPink
                            ),
                            modifier = Modifier.testTag("master_messaging_switch")
                        )
                    }
                }
            }

            // Per-App Controls & Policies
            item {
                Text(
                    text = "Supported Apps & Channels",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PlatformToggleChip(
                        platform = MessagingPlatform.WHATSAPP,
                        enabled = settings.isWhatsAppEnabled,
                        onToggle = { messagingViewModel.togglePlatform(MessagingPlatform.WHATSAPP, it) },
                        modifier = Modifier.weight(1f)
                    )
                    PlatformToggleChip(
                        platform = MessagingPlatform.TELEGRAM,
                        enabled = settings.isTelegramEnabled,
                        onToggle = { messagingViewModel.togglePlatform(MessagingPlatform.TELEGRAM, it) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PlatformToggleChip(
                        platform = MessagingPlatform.SMS,
                        enabled = settings.isSmsEnabled,
                        onToggle = { messagingViewModel.togglePlatform(MessagingPlatform.SMS, it) },
                        modifier = Modifier.weight(1f)
                    )
                    PlatformToggleChip(
                        platform = MessagingPlatform.MESSENGER,
                        enabled = settings.isMessengerEnabled,
                        onToggle = { messagingViewModel.togglePlatform(MessagingPlatform.MESSENGER, it) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Direct SMS Permission Settings Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Direct SMS Mode (Telephony Hardware)",
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (settings.isDirectSmsSendEnabled)
                                    "Sends directly via carrier. Default is safe system composer."
                                else "Default safe mode: Opens system SMS composer for review.",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = settings.isDirectSmsSendEnabled,
                            onCheckedChange = { enable ->
                                if (enable) {
                                    val hasPerm = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.SEND_SMS
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (hasPerm) {
                                        messagingViewModel.toggleDirectSms(true)
                                    } else {
                                        smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                                    }
                                } else {
                                    messagingViewModel.toggleDirectSms(false)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ElectricBlue
                            ),
                            modifier = Modifier.testTag("direct_sms_switch")
                        )
                    }
                }
            }

            // Command Simulator / Voice Input Tester
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = ElectricBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Test Voice Messaging Commands",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Supports English, Bengali (বাংলা), and Korean (한국어) natural commands.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick example prompts
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val samples = listOf(
                                "WhatsApp Alex: I'll be late by 10 minutes",
                                "হোয়াটসঅ্যাপে রাহিমকে মেসেজ পাঠাও: আমি ক্লাসে আছি",
                                "왓츠앱으로 민수에게 메시지 보내: 10분 뒤에 도착해",
                                "Telegram Michael: Check the doc please",
                                "Text John: Where are you?"
                            )
                            items(samples) { sample ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(DarkSurfaceVariant)
                                        .clickable {
                                            testInputText = sample
                                            messagingViewModel.handleVoiceInput(sample)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(text = sample, color = ElectricBlue, fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = testInputText,
                                onValueChange = { testInputText = it },
                                placeholder = { Text("Type or paste command...", color = TextSecondary, fontSize = 12.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricBlue,
                                    unfocusedBorderColor = DarkSurfaceVariant,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("test_messaging_input")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (testInputText.isNotBlank()) {
                                        messagingViewModel.handleVoiceInput(testInputText)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("parse_command_button")
                            ) {
                                Text("Parse", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Compact Preview Before Consequential Action
            if (isConfirmPreviewVisible && pendingCommand != null) {
                item {
                    val cmd = pendingCommand!!
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1830)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, NeonPink, RoundedCornerShape(16.dp))
                            .testTag("confirmation_preview_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = NeonPink)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Safety Confirmation Preview",
                                        color = NeonPink,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ElectricBlue.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = cmd.platform.displayName,
                                        color = ElectricBlue,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = "Recipient:", color = TextSecondary, fontSize = 12.sp)
                            Text(
                                text = cmd.recipient ?: "Unknown",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Exact Message Body:", color = TextSecondary, fontSize = 12.sp)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurface)
                                    .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = cmd.messageBody ?: "(Empty message)",
                                    color = TextPrimary,
                                    fontSize = 13.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "ⓘ Notice: Yuna will open ${cmd.platform.displayName} and prepare this draft. Messages are never sent secretly.",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = { messagingViewModel.cancelPending() },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                                    modifier = Modifier.testTag("cancel_dispatch_button")
                                ) {
                                    Text("Cancel")
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Button(
                                    onClick = {
                                        messagingViewModel.executeConfirmedDispatch(
                                            useAccessibility = YunaAccessibilityService.isConnected()
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                                    enabled = !isDispatching,
                                    modifier = Modifier.testTag("confirm_dispatch_button")
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Prepare & Dispatch", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Ambiguity Clarification Card
            if (isAmbiguityVisible && pendingCommand != null) {
                item {
                    val cmd = pendingCommand!!
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF16253A)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, WarningAmber, RoundedCornerShape(16.dp))
                            .testTag("ambiguity_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.QuestionMark, contentDescription = null, tint = WarningAmber)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Clarification Needed",
                                    color = WarningAmber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            if (cmd.isAmbiguousPlatform) {
                                Text(
                                    text = "Which app would you like to use?",
                                    color = TextPrimary,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { messagingViewModel.selectClarifiedPlatform(MessagingPlatform.WHATSAPP) },
                                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                                    ) { Text("WhatsApp", fontSize = 11.sp) }
                                    Button(
                                        onClick = { messagingViewModel.selectClarifiedPlatform(MessagingPlatform.TELEGRAM) },
                                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                                    ) { Text("Telegram", fontSize = 11.sp) }
                                    Button(
                                        onClick = { messagingViewModel.selectClarifiedPlatform(MessagingPlatform.SMS) },
                                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                                    ) { Text("SMS", fontSize = 11.sp) }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            if (cmd.isAmbiguousRecipient) {
                                Text(
                                    text = "Who is the recipient?",
                                    color = TextPrimary,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                if (cmd.candidateRecipients.isNotEmpty()) {
                                    Text("Did you mean:", color = TextSecondary, fontSize = 11.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        cmd.candidateRecipients.forEach { candidate ->
                                            OutlinedButton(
                                                onClick = { messagingViewModel.selectClarifiedRecipient(candidate) }
                                            ) {
                                                Text(candidate, color = ElectricBlue, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = clarifiedRecipientInput,
                                        onValueChange = { clarifiedRecipientInput = it },
                                        placeholder = { Text("Enter name or phone/handle...") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Button(
                                        onClick = {
                                            if (clarifiedRecipientInput.isNotBlank()) {
                                                messagingViewModel.selectClarifiedRecipient(clarifiedRecipientInput.trim())
                                                clarifiedRecipientInput = ""
                                            }
                                        }
                                    ) {
                                        Text("Set")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Last Action Result Card
            if (lastResult != null) {
                item {
                    val res = lastResult!!
                    val isSuccess = res.stage != MessagingExecutionStage.FAILED
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (isSuccess) SuccessGreen else ErrorRed, RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (isSuccess) SuccessGreen else ErrorRed
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Status: ${res.stage.label}",
                                        color = if (isSuccess) SuccessGreen else ErrorRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(text = res.platform.displayName, color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = res.summary, color = TextPrimary, fontSize = 13.sp)
                            if (res.details != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = res.details, color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Action History (Privacy-Preserving Log)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = TextSecondary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Action History (${actionLogs.size})",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = "Content not stored",
                        color = ElectricBlue.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }

            if (actionLogs.isEmpty()) {
                item {
                    Text(
                        text = "No messaging actions logged yet.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(actionLogs) { log ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = log.platform.displayName,
                                        color = ElectricBlue,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "→ ${log.recipientMasked}",
                                        color = TextPrimary,
                                        fontSize = 12.sp
                                    )
                                }
                                Text(
                                    text = log.summary,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = log.stage.label,
                                color = if (log.stage == MessagingExecutionStage.FAILED) ErrorRed else SuccessGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun PlatformToggleChip(
    platform: MessagingPlatform,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .border(
                1.dp,
                if (enabled) ElectricBlue.copy(alpha = 0.4f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .testTag("platform_toggle_${platform.name}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = platform.displayName,
                color = if (enabled) TextPrimary else TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ElectricBlue
                )
            )
        }
    }
}
