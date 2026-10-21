package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.YunaApplication
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NahidH4cksAccent
import com.example.ui.theme.NeonPink
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.YunaAssistantViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    assistantViewModel: YunaAssistantViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = YunaApplication.instance.preferencesDataStore
    val repo = YunaApplication.instance.geminiRepository
    val settings by assistantViewModel.settings.collectAsState()

    var customKeyInput by remember(settings.customApiKey) { mutableStateOf(settings.customApiKey) }
    var connectionTestStatus by remember { mutableStateOf<String?>(null) }
    var isTestingConnection by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "YUNA Settings",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ভাষা, পার্সোনালিটি ও সিস্টেম কনফিগারেশন",
                            color = ElectricBlue,
                            fontSize = 12.sp
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Language Selection
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = ElectricBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Language (ভাষা)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LanguageChip(
                                label = "Auto",
                                selected = settings.language == "auto",
                                onClick = { scope.launch { prefs.setLanguage("auto") } }
                            )
                            LanguageChip(
                                label = "বাংলা",
                                selected = settings.language == "bn",
                                onClick = { scope.launch { prefs.setLanguage("bn") } }
                            )
                            LanguageChip(
                                label = "한국어",
                                selected = settings.language == "ko",
                                onClick = { scope.launch { prefs.setLanguage("ko") } }
                            )
                            LanguageChip(
                                label = "English",
                                selected = settings.language == "en",
                                onClick = { scope.launch { prefs.setLanguage("en") } }
                            )
                        }
                    }
                }
            }

            // Personality Selection
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = NeonPink)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Personality Mode (ইউনার স্বভাব)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Modifies Yuna's prompt tone and conversational style directly.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        val modes = listOf("Friendly", "Calm", "Playful", "Concise", "Professional")
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            modes.chunked(3).forEach { rowModes ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    rowModes.forEach { mode ->
                                        FilterChip(
                                            selected = settings.personality == mode,
                                            onClick = { scope.launch { prefs.setPersonality(mode) } },
                                            label = { Text(mode) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = NeonPink,
                                                selectedLabelColor = Color.White,
                                                containerColor = DarkSurfaceVariant,
                                                labelColor = TextPrimary
                                            ),
                                            modifier = Modifier.testTag("personality_chip_$mode")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Voice & Speech Settings
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = ElectricBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Voice & Speech (ভয়েস কন্ট্রোল)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Speak AI Responses (অডিওতে উত্তর বলা)", color = TextPrimary, fontSize = 13.sp)
                            Switch(
                                checked = settings.ttsEnabled,
                                onCheckedChange = { scope.launch { prefs.setTtsEnabled(it) } },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = ElectricBlue
                                ),
                                modifier = Modifier.testTag("tts_enabled_switch")
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Speech Rate: ${String.format("%.1fx", settings.voiceSpeed)}",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Slider(
                            value = settings.voiceSpeed,
                            onValueChange = { scope.launch { prefs.setVoiceSpeed(it) } },
                            valueRange = 0.5f..1.5f,
                            steps = 9,
                            colors = SliderDefaults.colors(
                                thumbColor = ElectricBlue,
                                activeTrackColor = ElectricBlue
                            ),
                            modifier = Modifier.testTag("voice_speed_slider")
                        )
                    }
                }
            }

            // Reduced Motion Toggle
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Reduced Motion (কম অ্যানিমেশন)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Slows down character particles and breathing loops.", color = TextSecondary, fontSize = 12.sp)
                        }
                        Switch(
                            checked = settings.reducedMotion,
                            onCheckedChange = { scope.launch { prefs.setReducedMotion(it) } },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeonPink
                            ),
                            modifier = Modifier.testTag("reduced_motion_switch")
                        )
                    }
                }
            }

            // Gemini API Connection & Key Management
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = NahidH4cksAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gemini AI API Configuration", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Keys can be provided via Secrets panel (BuildConfig) or entered manually below.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = customKeyInput,
                            onValueChange = {
                                customKeyInput = it
                                scope.launch { prefs.setCustomApiKey(it) }
                            },
                            placeholder = { Text("Custom Gemini API Key (optional)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = DarkSurfaceVariant,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_api_key_input")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                isTestingConnection = true
                                connectionTestStatus = "Testing connection to Gemini 2.5 Flash..."
                                scope.launch {
                                    val result = repo.sendMessage("Ping test: confirm connection.")
                                    isTestingConnection = false
                                    connectionTestStatus = if (result.isOnlineAi) {
                                        "✓ Success: Connected to Gemini AI! (Response: '${result.text.take(40)}...')"
                                    } else {
                                        "⚠ Offline fallback active: ${result.error ?: "Using local AI engine."}"
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("test_api_connection_button")
                        ) {
                            Text(
                                text = if (isTestingConnection) "Testing..." else "Test Real API Connection",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (connectionTestStatus != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = connectionTestStatus ?: "",
                                color = if (connectionTestStatus?.startsWith("✓") == true) SuccessGreen else WarningAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Creator Branding
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = NahidH4cksAccent)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Nahid H4cks",
                                color = NahidH4cksAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "YUNA AI (유나 AI) v2.1 • Native Android Assistant",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
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
fun LanguageChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ElectricBlue,
            selectedLabelColor = Color.Black,
            containerColor = DarkSurfaceVariant,
            labelColor = TextPrimary
        ),
        modifier = Modifier.testTag("lang_chip_$label")
    )
}
