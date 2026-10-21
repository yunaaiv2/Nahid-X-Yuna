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
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NeonPink
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.QuickNotesViewModel
import com.example.ui.viewmodel.YunaAssistantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneActionsScreen(
    assistantViewModel: YunaAssistantViewModel,
    notesViewModel: QuickNotesViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by assistantViewModel.uiState.collectAsState()
    val notes by notesViewModel.notes.collectAsState()

    var newNoteTitle by remember { mutableStateOf("") }
    var newNoteContent by remember { mutableStateOf("") }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Phone Actions Hub",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "সরাসরি ফোন ও স্ক্রিন নিয়ন্ত্রণ",
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
                actions = {
                    IconButton(
                        onClick = { assistantViewModel.emergencyStop() },
                        modifier = Modifier.testTag("emergency_stop_button")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Status Banner
            item {
                Spacer(modifier = Modifier.height(4.dp))
                if (uiState.lastActionResult != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF142436))
                            .border(1.dp, ElectricBlue, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = uiState.lastActionResult ?: "",
                            color = ElectricBlue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Quick Device Controls Section
            item {
                SectionHeader("Device Controls (ডিভাইস কন্ট্রোল)")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionTile(
                        title = "Torch",
                        icon = Icons.Default.FlashlightOn,
                        color = NeonPink,
                        modifier = Modifier.weight(1f)
                    ) {
                        assistantViewModel.executeActionTag("ACTION:FLASHLIGHT_ON")
                    }
                    ActionTile(
                        title = "Torch Off",
                        icon = Icons.Default.FlashlightOn,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f)
                    ) {
                        assistantViewModel.executeActionTag("ACTION:FLASHLIGHT_OFF")
                    }
                    ActionTile(
                        title = "Battery",
                        icon = Icons.Default.BatteryChargingFull,
                        color = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    ) {
                        assistantViewModel.executeActionTag("ACTION:BATTERY")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionTile(
                        title = "Vol Up",
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        color = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    ) {
                        assistantViewModel.executeActionTag("ACTION:VOLUME_UP")
                    }
                    ActionTile(
                        title = "Vol Down",
                        icon = Icons.AutoMirrored.Filled.VolumeDown,
                        color = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    ) {
                        assistantViewModel.executeActionTag("ACTION:VOLUME_DOWN")
                    }
                    ActionTile(
                        title = "Date/Time",
                        icon = Icons.Default.Calculate,
                        color = NeonPink,
                        modifier = Modifier.weight(1f)
                    ) {
                        assistantViewModel.executeActionTag("ACTION:TIME")
                    }
                }
            }

            // Screen Companion Section
            item {
                SectionHeader("Screen Companion (স্ক্রিন অটোমেশন)")
                Text(
                    text = if (uiState.isAccessibilityActive)
                        "● Accessibility Service is active and ready."
                    else
                        "⚠ Accessibility Service inactive. Enable 'YUNA AI Screen Companion' in Settings.",
                    color = if (uiState.isAccessibilityActive) SuccessGreen else ErrorRed,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionTile(
                        title = "Go Back",
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        color = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    ) {
                        assistantViewModel.executeActionTag("ACTION:NAV_BACK")
                    }
                    ActionTile(
                        title = "Go Home",
                        icon = Icons.Default.Home,
                        color = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    ) {
                        assistantViewModel.executeActionTag("ACTION:NAV_HOME")
                    }
                    ActionTile(
                        title = "Read Screen",
                        icon = Icons.Default.Visibility,
                        color = NeonPink,
                        modifier = Modifier.weight(1f)
                    ) {
                        assistantViewModel.executeActionTag("ACTION:READ_SCREEN")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionTile(
                        title = "Scroll Down",
                        icon = Icons.Default.KeyboardArrowDown,
                        color = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    ) {
                        assistantViewModel.executeActionTag("ACTION:SCROLL_DOWN")
                    }
                    ActionTile(
                        title = "Scroll Up",
                        icon = Icons.Default.KeyboardArrowUp,
                        color = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    ) {
                        assistantViewModel.executeActionTag("ACTION:SCROLL_UP")
                    }
                }
            }

            // Quick App Launchers
            item {
                SectionHeader("Launch Applications (অ্যাপ চালান)")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppChip("YouTube") { assistantViewModel.executeActionTag("ACTION:OPEN_APP:youtube") }
                    AppChip("Chrome") { assistantViewModel.executeActionTag("ACTION:OPEN_APP:chrome") }
                    AppChip("Settings") { assistantViewModel.executeActionTag("ACTION:OPEN_APP:settings") }
                    AppChip("Camera") { assistantViewModel.executeActionTag("ACTION:OPEN_APP:camera") }
                }
            }

            // Local Quick Notes
            item {
                SectionHeader("Local Quick Notes (অফলাইন নোট)")
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        OutlinedTextField(
                            value = newNoteTitle,
                            onValueChange = { newNoteTitle = it },
                            label = { Text("Note Title") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = DarkSurfaceVariant,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("note_title_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newNoteContent,
                            onValueChange = { newNoteContent = it },
                            label = { Text("Note Content...") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = DarkSurfaceVariant,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("note_content_input")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                notesViewModel.addNote(newNoteTitle, newNoteContent)
                                newNoteTitle = ""
                                newNoteContent = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                            modifier = Modifier
                                .align(Alignment.End)
                                .testTag("save_note_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Note", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Existing Notes List
            items(notes.size) { index ->
                val note = notes[index]
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
                            Text(text = note.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = note.content, color = TextSecondary, fontSize = 12.sp)
                        }
                        IconButton(
                            onClick = { notesViewModel.deleteNote(note.id) },
                            modifier = Modifier.testTag("delete_note_${note.id}")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = TextPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun ActionTile(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .height(72.dp)
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .testTag("action_tile_$title")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun AppChip(name: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .testTag("app_chip_$name")
    ) {
        Text(text = name, color = ElectricBlue, fontSize = 12.sp)
    }
}
