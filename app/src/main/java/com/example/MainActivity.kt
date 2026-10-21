package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.ChatHistoryScreen
import com.example.ui.screens.MainAssistantScreen
import com.example.ui.screens.MessagingScreen
import com.example.ui.screens.PermissionsScreen
import com.example.ui.screens.PhoneActionsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SmartMemoryScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.YunaAssistantViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    YunaAppNavGraph()
                }
            }
        }
    }
}

@Composable
fun YunaAppNavGraph(
    assistantViewModel: YunaAssistantViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home",
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        composable("home") {
            MainAssistantScreen(
                viewModel = assistantViewModel,
                onNavigatePermissions = { navController.navigate("permissions") },
                onNavigatePhoneActions = { navController.navigate("phone_actions") },
                onNavigateSmartMemory = { navController.navigate("smart_memory") },
                onNavigateChatHistory = { navController.navigate("chat_history") },
                onNavigateSettings = { navController.navigate("settings") },
                onNavigateMessaging = { navController.navigate("messaging") }
            )
        }

        composable("permissions") {
            PermissionsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("phone_actions") {
            PhoneActionsScreen(
                assistantViewModel = assistantViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("smart_memory") {
            SmartMemoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("chat_history") {
            ChatHistoryScreen(
                assistantViewModel = assistantViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen(
                assistantViewModel = assistantViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("messaging") {
            MessagingScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
