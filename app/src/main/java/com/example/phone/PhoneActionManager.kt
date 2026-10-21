package com.example.phone

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.AlarmClock
import android.provider.Settings
import com.example.accessibility.YunaAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class ActionResult {
    data class Success(val message: String) : ActionResult()
    data class Failure(val reason: String) : ActionResult()
}

class PhoneActionManager(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private var isTorchOn = false

    fun toggleTorch(enable: Boolean? = null): ActionResult {
        if (cameraManager == null) {
            return ActionResult.Failure("Camera service unavailable on this device.")
        }
        return try {
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
            if (cameraId == null) {
                return ActionResult.Failure("No camera with flash detected.")
            }
            val targetState = enable ?: !isTorchOn
            cameraManager.setTorchMode(cameraId, targetState)
            isTorchOn = targetState
            val stateText = if (targetState) "turned ON" else "turned OFF"
            ActionResult.Success("Flashlight is now $stateText.")
        } catch (e: Exception) {
            ActionResult.Failure("Flashlight error: ${e.localizedMessage}")
        }
    }

    fun getBatteryStatus(): ActionResult {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        if (batteryManager != null) {
            val level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            val isCharging = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS) == BatteryManager.BATTERY_STATUS_CHARGING
            } else false
            val chargingStatus = if (isCharging) " (Charging)" else " (Discharging)"
            return ActionResult.Success("Battery is at $level%$chargingStatus.")
        }
        return ActionResult.Failure("Battery status could not be read.")
    }

    fun getCurrentDateTime(): ActionResult {
        val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy 'at' h:mm a", Locale.getDefault())
        val formatted = sdf.format(Date())
        return ActionResult.Success("Current time: $formatted")
    }

    fun adjustVolume(direction: Int): ActionResult {
        // direction: 1 for up, -1 for down, 0 for mute
        if (audioManager == null) return ActionResult.Failure("Audio service unavailable.")
        return try {
            val adjust = when {
                direction > 0 -> AudioManager.ADJUST_RAISE
                direction < 0 -> AudioManager.ADJUST_LOWER
                else -> AudioManager.ADJUST_SAME
            }
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, adjust, AudioManager.FLAG_SHOW_UI)
            val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            ActionResult.Success("Media volume adjusted to $current/$max.")
        } catch (e: Exception) {
            ActionResult.Failure("Volume adjustment failed: ${e.localizedMessage}")
        }
    }

    fun openApp(appNameOrPackage: String): ActionResult {
        val pm = context.packageManager
        val query = appNameOrPackage.lowercase(Locale.ROOT).trim()

        val packageMap = mapOf(
            "youtube" to "com.google.android.youtube",
            "chrome" to "com.android.chrome",
            "browser" to "com.android.chrome",
            "settings" to "com.android.settings",
            "camera" to "android.media.action.IMAGE_CAPTURE",
            "maps" to "com.google.android.apps.maps",
            "play store" to "com.android.vending",
            "clock" to "com.google.android.deskclock",
            "calculator" to "com.google.android.calculator",
            "whatsapp" to "com.whatsapp"
        )

        val targetPackage = packageMap[query] ?: query

        // Try direct launch intent
        val launchIntent = pm.getLaunchIntentForPackage(targetPackage)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            return ActionResult.Success("Opening $query...")
        }

        // Try query installed apps by label
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        for (app in installedApps) {
            val label = pm.getApplicationLabel(app).toString().lowercase(Locale.ROOT)
            if (label.contains(query) || query.contains(label)) {
                val intent = pm.getLaunchIntentForPackage(app.packageName)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return ActionResult.Success("Opening ${pm.getApplicationLabel(app)}...")
                }
            }
        }

        // Fallback for settings or camera
        if (query.contains("setting")) {
            val settingsIntent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(settingsIntent)
            return ActionResult.Success("Opened System Settings.")
        }

        return ActionResult.Failure("Could not find installed app matching '$appNameOrPackage'.")
    }

    fun openWebSearchOrUrl(urlOrQuery: String): ActionResult {
        val uri = if (urlOrQuery.startsWith("http://") || urlOrQuery.startsWith("https://")) {
            Uri.parse(urlOrQuery)
        } else {
            Uri.parse("https://www.google.com/search?q=${Uri.encode(urlOrQuery)}")
        }
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            ActionResult.Success("Opening link...")
        } catch (e: Exception) {
            ActionResult.Failure("Failed to open browser: ${e.localizedMessage}")
        }
    }

    fun openAlarmClock(hour: Int? = null, minutes: Int? = null, message: String = "Yuna Alarm"): ActionResult {
        val intent = if (hour != null && minutes != null) {
            Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minutes)
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        return try {
            context.startActivity(intent)
            ActionResult.Success(if (hour != null) "Setting alarm for $hour:$minutes..." else "Opening Alarms...")
        } catch (e: Exception) {
            ActionResult.Failure("Could not open clock app: ${e.localizedMessage}")
        }
    }

    fun performAccessibilityAction(actionType: String, argument: String? = null): ActionResult {
        val service = YunaAccessibilityService.serviceInstance
            ?: return ActionResult.Failure("Accessibility Service is not enabled. Please enable 'YUNA AI Screen Companion' in Settings -> Accessibility.")

        return when (actionType.lowercase(Locale.ROOT)) {
            "back" -> {
                val ok = service.executeBack()
                if (ok) ActionResult.Success("Navigated Back.") else ActionResult.Failure("Could not navigate back.")
            }
            "home" -> {
                val ok = service.executeHome()
                if (ok) ActionResult.Success("Returned to Home Screen.") else ActionResult.Failure("Could not return to Home screen.")
            }
            "scroll_down" -> {
                val ok = service.scroll(forward = true)
                if (ok) ActionResult.Success("Scrolled Down.") else ActionResult.Failure("Scroll failed or no scrollable view visible.")
            }
            "scroll_up" -> {
                val ok = service.scroll(forward = false)
                if (ok) ActionResult.Success("Scrolled Up.") else ActionResult.Failure("Scroll failed or no scrollable view visible.")
            }
            "click_text" -> {
                if (argument.isNullOrBlank()) ActionResult.Failure("No target text specified.")
                else {
                    val ok = service.findAndClickByText(argument)
                    if (ok) ActionResult.Success("Clicked element containing '$argument'.")
                    else ActionResult.Failure("Could not find clickable element matching '$argument'.")
                }
            }
            "read_screen" -> {
                val textItems = service.readVisibleText()
                if (textItems.isNotEmpty()) {
                    val summary = textItems.take(8).joinToString("\n• ")
                    ActionResult.Success("Visible text:\n• $summary")
                } else {
                    ActionResult.Failure("No accessible text found on current screen.")
                }
            }
            else -> ActionResult.Failure("Unknown accessibility action: $actionType")
        }
    }

    fun calculateMath(expression: String): ActionResult {
        return try {
            val cleaned = expression.replace("x", "*").replace("×", "*").replace("÷", "/").trim()
            val result = evaluateSimpleExpression(cleaned)
            ActionResult.Success("Result: $expression = $result")
        } catch (e: Exception) {
            ActionResult.Failure("Could not calculate expression: ${e.localizedMessage}")
        }
    }

    private fun evaluateSimpleExpression(expr: String): Double {
        // Evaluates basic math (+, -, *, /)
        val sanitized = expr.replace(" ", "")
        val tokens = mutableListOf<String>()
        var currentNum = StringBuilder()
        for (ch in sanitized) {
            if (ch in "+-*/") {
                if (currentNum.isNotEmpty()) {
                    tokens.add(currentNum.toString())
                    currentNum = StringBuilder()
                }
                tokens.add(ch.toString())
            } else if (ch.isDigit() || ch == '.') {
                currentNum.append(ch)
            }
        }
        if (currentNum.isNotEmpty()) tokens.add(currentNum.toString())

        if (tokens.isEmpty()) throw IllegalArgumentException("Empty expression")

        // First pass: * and /
        val intermediate = mutableListOf<String>()
        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]
            if (token == "*" || token == "/") {
                val prev = intermediate.removeAt(intermediate.size - 1).toDouble()
                val next = tokens[i + 1].toDouble()
                val res = if (token == "*") prev * next else prev / next
                intermediate.add(res.toString())
                i += 2
            } else {
                intermediate.add(token)
                i++
            }
        }

        // Second pass: + and -
        var total = intermediate[0].toDouble()
        var j = 1
        while (j < intermediate.size) {
            val op = intermediate[j]
            val next = intermediate[j + 1].toDouble()
            total = if (op == "+") total + next else total - next
            j += 2
        }
        return total
    }
}
