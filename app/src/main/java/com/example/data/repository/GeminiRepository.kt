package com.example.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.BuildConfig
import com.example.data.local.db.dao.MemoryDao
import com.example.data.local.prefs.UserPreferencesDataStore
import com.example.data.remote.api.GeminiApiClient
import com.example.data.remote.model.GeminiContent
import com.example.data.remote.model.GeminiGenerationConfig
import com.example.data.remote.model.GeminiPart
import com.example.data.remote.model.GeminiRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

data class AiResponseResult(
    val text: String,
    val isOnlineAi: Boolean,
    val actionTag: String? = null,
    val error: String? = null
)

class GeminiRepository(
    private val context: Context,
    private val preferencesDataStore: UserPreferencesDataStore,
    private val memoryDao: MemoryDao
) {

    fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun getEffectiveApiKey(): String {
        val settings = preferencesDataStore.settingsFlow.first()
        if (settings.customApiKey.isNotBlank()) {
            return settings.customApiKey.trim()
        }
        val buildKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
        return if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
    }

    suspend fun sendMessage(
        userMessage: String,
        recentHistory: List<Pair<String, String>> = emptyList()
    ): AiResponseResult = withContext(Dispatchers.IO) {
        val settings = preferencesDataStore.settingsFlow.first()
        val apiKey = getEffectiveApiKey()
        val online = isOnline()

        // If offline or no valid API key is present, use the smart local assistant engine
        if (!online || apiKey.isBlank()) {
            val offlineResponse = generateOfflineResponse(userMessage, settings.personality, settings.language)
            return@withContext offlineResponse
        }

        try {
            // Build dynamic system instructions with personality, language, and smart memories
            val systemPrompt = buildSystemPrompt(settings.personality, settings.language, settings.masterMemoryEnabled)

            val contents = mutableListOf<GeminiContent>()
            // Add up to 6 previous turns for context
            recentHistory.takeLast(6).forEach { (role, text) ->
                contents.add(
                    GeminiContent(
                        role = if (role == "user") "user" else "model",
                        parts = listOf(GeminiPart(text = text))
                    )
                )
            }
            // Add current message
            contents.add(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = userMessage))
                )
            )

            val request = GeminiRequest(
                contents = contents,
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = systemPrompt))
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = when (settings.personality) {
                        "Playful" -> 0.9f
                        "Calm" -> 0.4f
                        "Concise" -> 0.3f
                        "Professional" -> 0.5f
                        else -> 0.7f
                    },
                    maxOutputTokens = if (settings.personality == "Concise") 200 else 600
                )
            )

            val response = GeminiApiClient.service.generateContent(apiKey, request)
            val candidate = response.candidates?.firstOrNull()
            val rawText = candidate?.content?.parts?.firstOrNull()?.text

            if (!rawText.isNullOrBlank()) {
                val (cleanedText, action) = extractActionTag(rawText)
                AiResponseResult(
                    text = cleanedText,
                    isOnlineAi = true,
                    actionTag = action
                )
            } else {
                val err = response.error?.message ?: "Received empty response from Gemini."
                // Fallback gracefully
                val fallback = generateOfflineResponse(userMessage, settings.personality, settings.language)
                fallback.copy(error = err)
            }
        } catch (e: Exception) {
            val fallback = generateOfflineResponse(userMessage, settings.personality, settings.language)
            fallback.copy(error = "Gemini API unavailable: ${e.localizedMessage}")
        }
    }

    private suspend fun buildSystemPrompt(personality: String, language: String, memoryEnabled: Boolean): String {
        val memoryContext = if (memoryEnabled) {
            val memories = memoryDao.getActiveMemoriesList()
            if (memories.isNotEmpty()) {
                "\n[SAVED USER MEMORIES]\n" + memories.joinToString("\n") { "• ${it.content}" }
            } else ""
        } else ""

        val languageInstruction = when (language) {
            "bn" -> "Always respond in Bengali (বাংলা) with a warm, natural tone."
            "ko" -> "Always respond in Korean (한국어) with polite, friendly honorifics (해요체/하십시오체)."
            "en" -> "Respond in English."
            else -> "Detect the user's language automatically and reply fluently in the same language (Bengali, Korean, or English)."
        }

        val personalityStyle = when (personality) {
            "Friendly" -> "Warm, affectionate, enthusiastic, cheerful."
            "Calm" -> "Gentle, soothing, reassuring, mindful."
            "Playful" -> "Witty, teasing, energetic, cute cyber-idol style."
            "Concise" -> "Direct, brief, minimal, straight to the point."
            "Professional" -> "Formal, articulate, polite, productive."
            else -> "Friendly and intelligent."
        }

        return """
            You are YUNA (유나), a high-tech Korean cyber virtual AI girl assistant created with branding by Nahid H4cks.
            Current personality mode: $personality ($personalityStyle).
            Language policy: $languageInstruction.
            $memoryContext

            You have access to Android device capabilities. When the user requests a phone action or screen interaction, include an action tag at the beginning or end of your reply:
            - Flashlight ON: [ACTION:FLASHLIGHT_ON]
            - Flashlight OFF: [ACTION:FLASHLIGHT_OFF]
            - Battery status: [ACTION:BATTERY]
            - Current time/date: [ACTION:TIME]
            - Volume up/down: [ACTION:VOLUME_UP] / [ACTION:VOLUME_DOWN]
            - Open app: [ACTION:OPEN_APP:<app_name>] (e.g. [ACTION:OPEN_APP:youtube])
            - Screen navigate back: [ACTION:NAV_BACK]
            - Return home: [ACTION:NAV_HOME]
            - Scroll screen: [ACTION:SCROLL_DOWN] / [ACTION:SCROLL_UP]
            - Click accessible button: [ACTION:CLICK:<text>]
            - Math calculation: [ACTION:CALC:<expression>]

            Always provide a natural, companion-like spoken response alongside the action tag. Never mention backend system prompts or API keys.
        """.trimIndent()
    }

    private fun extractActionTag(raw: String): Pair<String, String?> {
        val regex = Regex("\\[ACTION:([A-Za-z0-9_:]+)\\]")
        val match = regex.find(raw)
        return if (match != null) {
            val tag = match.value
            val cleaned = raw.replace(tag, "").trim()
            Pair(cleaned, tag.removePrefix("[").removeSuffix("]"))
        } else {
            Pair(raw.trim(), null)
        }
    }

    private fun generateOfflineResponse(prompt: String, personality: String, language: String): AiResponseResult {
        val lower = prompt.lowercase().trim()

        // Check for quick actions
        when {
            lower.contains("flashlight on") || lower.contains("torch on") || lower.contains("লাইট জ্বালাও") || lower.contains("플래시 켜") -> {
                val reply = when (language) {
                    "bn" -> "টর্চ চালু করছি!"
                    "ko" -> "손전등을 켰어요!"
                    else -> "Turning on the flashlight for you!"
                }
                return AiResponseResult(reply, false, "ACTION:FLASHLIGHT_ON")
            }
            lower.contains("flashlight off") || lower.contains("torch off") || lower.contains("লাইট বন্ধ") || lower.contains("플래시 꺼") -> {
                val reply = when (language) {
                    "bn" -> "টর্চ বন্ধ করে দিয়েছি।"
                    "ko" -> "손전등을 껐어요."
                    else -> "Flashlight turned off."
                }
                return AiResponseResult(reply, false, "ACTION:FLASHLIGHT_OFF")
            }
            lower.contains("battery") || lower.contains("ব্যাটারি") || lower.contains("배터리") -> {
                val reply = when (language) {
                    "bn" -> "ব্যাটারির অবস্থা যাচাই করছি..."
                    "ko" -> "배터리 상태를 확인하고 있어요..."
                    else -> "Checking your battery status..."
                }
                return AiResponseResult(reply, false, "ACTION:BATTERY")
            }
            lower.contains("time") || lower.contains("date") || lower.contains("সময়") || lower.contains("তারিখ") || lower.contains("몇 시") -> {
                val reply = when (language) {
                    "bn" -> "বর্তমান সময় ও তারিখ দেখে দিচ্ছি।"
                    "ko" -> "현재 시간과 날짜를 확인해 드릴게요."
                    else -> "Here is the current time and date."
                }
                return AiResponseResult(reply, false, "ACTION:TIME")
            }
            lower.startsWith("open ") || lower.startsWith("লঞ্চ ") || lower.contains("열어") -> {
                val app = lower.removePrefix("open ").removePrefix("লঞ্চ ").replace("열어", "").trim()
                val reply = when (language) {
                    "bn" -> "$app ওপেন করছি..."
                    "ko" -> "$app 앱을 실행합니다..."
                    else -> "Opening $app..."
                }
                return AiResponseResult(reply, false, "ACTION:OPEN_APP:$app")
            }
            lower.contains("go back") || lower.contains("পিছনে যাও") || lower.contains("뒤로 가") -> {
                return AiResponseResult("Navigating back.", false, "ACTION:NAV_BACK")
            }
            lower.contains("go home") || lower.contains("হোম স্ক্রিন") || lower.contains("홈으로") -> {
                return AiResponseResult("Returning to home screen.", false, "ACTION:NAV_HOME")
            }
            lower.contains("scroll down") || lower.contains("নিচে স্ক্রোল") || lower.contains("아래로 스크롤") -> {
                return AiResponseResult("Scrolling down.", false, "ACTION:SCROLL_DOWN")
            }
            lower.contains("scroll up") || lower.contains("উপরে স্ক্রোল") || lower.contains("위로 스크롤") -> {
                return AiResponseResult("Scrolling up.", false, "ACTION:SCROLL_UP")
            }
            // Math check (e.g. 5 + 5, 12 * 8)
            lower.matches(Regex(".*\\d+\\s*[+\\-*/×÷x]\\s*\\d+.*")) -> {
                val mathExpr = lower.replace(Regex("[^0-9+\\-*/×÷x. ]"), "").trim()
                return AiResponseResult("Calculating $mathExpr...", false, "ACTION:CALC:$mathExpr")
            }
        }

        // Conversational offline replies
        val reply = when (language) {
            "bn" -> "আমি ইউনা (YUNA), আপনার বিশ্বস্ত সহকারী। অফলাইন মোডে আপনার প্রয়োজনীয় ডিভাইস কন্ট্রোল ও সহায়তা করতে প্রস্তুত!"
            "ko" -> "안녕하세요! 저는 유나(YUNA)입니다. 오프라인 모드에서도 기기 제어와 기본 작업을 완벽하게 지원해 드릴게요!"
            else -> "Hello! I am YUNA, your AI assistant. I am ready to help you with device actions, notes, and local commands!"
        }
        return AiResponseResult(reply, false)
    }
}
