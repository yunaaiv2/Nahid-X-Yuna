package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.accessibility.YunaAccessibilityService
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueGlow
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NeonPink
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

data class PermissionItem(
    val id: String,
    val title: String,
    val descriptionEn: String,
    val descriptionBn: String,
    val icon: ImageVector,
    val isGranted: Boolean,
    val isSpecialAccess: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    var hasOverlayPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else true
        )
    }

    var hasAccessibilityPermission by remember {
        mutableStateOf(YunaAccessibilityService.isConnected())
    }

    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    fun recheckPermissions() {
        hasMicPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true
        hasCameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        hasOverlayPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else true
        hasAccessibilityPermission = YunaAccessibilityService.isConnected()
    }

    val items = listOf(
        PermissionItem(
            id = "internet",
            title = "Internet Access (ইন্টারনেট সংযোগ)",
            descriptionEn = "Allows Yuna to connect to Gemini AI models and retrieve real-time intelligence.",
            descriptionBn = "ইউনাকে জেমিনাই এআই মডেলের সাথে সংযোগ করে বুদ্ধিমান উত্তর প্রদানের সুযোগ দেয়।",
            icon = Icons.Default.Wifi,
            isGranted = true
        ),
        PermissionItem(
            id = "mic",
            title = "Microphone (মাইক্রোফোন)",
            descriptionEn = "Used strictly when you tap the mic to speak to Yuna. Never records silently.",
            descriptionBn = "ইউনার সাথে কথা বলার জন্য ব্যবহৃত হয়। গোপনে কোনো অডিও রেকর্ড করা হয় না।",
            icon = Icons.Default.Mic,
            isGranted = hasMicPermission
        ),
        PermissionItem(
            id = "overlay",
            title = "Floating Assistant (ফ্লোটিং অ্যাসিস্ট্যান্ট)",
            descriptionEn = "Enables Yuna's floating bubble to stay on screen over other applications for quick access.",
            descriptionBn = "অন্যান্য অ্যাপের উপর ইউনার ফ্লোটিং বাবল প্রদর্শন করে তাৎক্ষণিক কমান্ড দিতে সাহায্য করে।",
            icon = Icons.Default.Layers,
            isGranted = hasOverlayPermission,
            isSpecialAccess = true
        ),
        PermissionItem(
            id = "accessibility",
            title = "Screen Companion (স্ক্রিন অটোমেশন)",
            descriptionEn = "Enables user-authorized actions like clicking buttons, scrolling, and navigating back/home.",
            descriptionBn = "ব্যবহারকারীর নির্দেশে স্ক্রিনের বোতাম চাপানো, স্ক্রোল করা ও পেছনের পেজে যাওয়ার ক্ষমতা প্রদান করে।",
            icon = Icons.Default.Accessibility,
            isGranted = hasAccessibilityPermission,
            isSpecialAccess = true
        ),
        PermissionItem(
            id = "notifications",
            title = "Notifications (নোটিফিকেশন)",
            descriptionEn = "Keeps the assistant service alive and shows floating assistant active status.",
            descriptionBn = "অ্যাসিস্ট্যান্ট সার্ভিস সচল রাখতে এবং ব্যাকগ্রাউন্ড নোটিফিকেশন প্রদর্শন করতে প্রয়োজন।",
            icon = Icons.Default.Notifications,
            isGranted = hasNotificationPermission
        ),
        PermissionItem(
            id = "camera",
            title = "Camera / Flashlight (ক্যামেরা ও ফ্ল্যাশলাইট)",
            descriptionEn = "Used safely to control the device flashlight/torch upon voice commands.",
            descriptionBn = "ভয়েস কমান্ডের মাধ্যমে ফোনের ফ্ল্যাশলাইট চালু বা বন্ধ করতে ব্যবহৃত হয়।",
            icon = Icons.Default.CameraAlt,
            isGranted = hasCameraPermission
        )
    )

    val grantedCount = items.count { it.isGranted }
    val totalCount = items.size

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "YUNA Permissions Center",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "পারমিশন ম্যানেজার ও নিরাপত্তা নিয়ন্ত্রণ",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                )
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
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Readiness score banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF1E1533),
                                    Color(0xFF102133)
                                )
                            )
                        )
                        .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (grantedCount >= 4) SuccessGreen.copy(alpha = 0.2f) else WarningAmber.copy(alpha = 0.2f))
                                .border(2.dp, if (grantedCount >= 4) SuccessGreen else WarningAmber, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$grantedCount/$totalCount",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Assistant Readiness",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = if (grantedCount >= 4) "Yuna has optimal permissions to assist you."
                                else "Enable permissions below for full native capabilities.",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            items(items.size) { index ->
                val perm = items[index]
                PermissionCard(
                    item = perm,
                    onRequest = {
                        when (perm.id) {
                            "mic" -> micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            "notifications" -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                            "camera" -> cameraLauncher.launch(Manifest.permission.CAMERA)
                            "overlay" -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                }
                            }
                            "accessibility" -> {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                context.startActivity(intent)
                            }
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { recheckPermissions() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recheck_permissions_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ElectricBlue
                    )
                ) {
                    Text("Recheck Permissions (পুনরায় যাচাই করুন)")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun PermissionCard(
    item: PermissionItem,
    onRequest: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = DarkSurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (item.isGranted) SuccessGreen.copy(alpha = 0.4f) else GlassBorder,
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (item.isGranted) SuccessGreen.copy(alpha = 0.15f) else ElectricBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = if (item.isGranted) SuccessGreen else ElectricBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = if (item.isGranted) "Granted (অনুমোদিত)" else "Not Granted (অনুমোদন প্রয়োজন)",
                        color = if (item.isGranted) SuccessGreen else WarningAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (item.isGranted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Granted",
                        tint = SuccessGreen,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Button(
                        onClick = onRequest,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonPink
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("grant_${item.id}_button")
                    ) {
                        Text(
                            text = if (item.isSpecialAccess) "Settings" else "Grant",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.descriptionEn,
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.descriptionBn,
                color = ElectricBlue.copy(alpha = 0.85f),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}
