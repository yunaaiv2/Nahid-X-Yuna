package com.example.ui.viewmodel

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.YunaApplication
import com.example.accessibility.YunaAccessibilityService
import com.example.data.local.db.entities.ChatMessageEntity
import com.example.data.local.db.entities.ConversationEntity
import com.example.data.local.prefs.YunaSettings
import com.example.floating.FloatingYunaService
import com.example.phone.ActionResult
import com.example.voice.VoiceState
import com.example.voice.YunaVoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AssistantUiState(
    val currentConversationId: Long = 1L,
    val messages: List<ChatMessageEntity> = emptyList(),
    val isOnline: Boolean = true,
    val voiceState: VoiceState = VoiceState.IDLE,
    val audioAmplitude: Float = 0f,
    val isAccessibilityActive: Boolean = false,
    val isFloatingActive: Boolean = false,
    val lastActionResult: String? = null,
    val isProcessing: Boolean = false,
    val errorMessage: String? = null
)

class YunaAssistantViewModel : ViewModel() {

    private val app = YunaApplication.instance
    private val chatDao = app.database.chatDao()
    private val repo = app.geminiRepository
    private val phoneManager = app.phoneActionManager

    val settings: StateFlow<YunaSettings> = app.preferencesDataStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, YunaSettings())

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    private var voiceManager: YunaVoiceManager? = null

    init {
        initVoiceManager()
        observeAccessibility()
        loadOrCreateDefaultConversation()
    }

    private fun initVoiceManager() {
        voiceManager = YunaVoiceManager(app.applicationContext).apply {
            onSpeechRecognized = { text ->
                if (text.isNotBlank()) {
                    sendMessage(text)
                }
            }
            onError = { err ->
                _uiState.value = _uiState.value.copy(errorMessage = err)
            }
        }

        viewModelScope.launch {
            voiceManager?.voiceState?.collect { state ->
                _uiState.value = _uiState.value.copy(voiceState = state)
            }
        }
        viewModelScope.launch {
            voiceManager?.audioAmplitude?.collect { amp ->
                _uiState.value = _uiState.value.copy(audioAmplitude = amp)
            }
        }
    }

    private fun observeAccessibility() {
        viewModelScope.launch {
            YunaAccessibilityService.isServiceActive.collect { active ->
                _uiState.value = _uiState.value.copy(isAccessibilityActive = active)
            }
        }
    }

    private fun loadOrCreateDefaultConversation() {
        viewModelScope.launch {
            val convos = chatDao.getAllConversations()
            convos.collect { list ->
                if (list.isEmpty()) {
                    val newId = chatDao.insertConversation(
                        ConversationEntity(title = "Welcome with Yuna")
                    )
                    _uiState.value = _uiState.value.copy(currentConversationId = newId)
                    // Insert introductory greeting
                    chatDao.insertMessage(
                        ChatMessageEntity(
                            conversationId = newId,
                            role = "model",
                            content = "안녕하세요! Hello! আমি ইউনা (YUNA), আপনার ব্যক্তিগত AI সহচর। আপনি আমাকে যেকোনো প্রশ্ন করতে পারেন, ভয়েস কমান্ড দিতে পারেন অথবা ফোনের কাজ নিয়ন্ত্রণ করতে বলতে পারেন।"
                        )
                    )
                } else if (_uiState.value.messages.isEmpty()) {
                    val activeId = list.first().id
                    _uiState.value = _uiState.value.copy(currentConversationId = activeId)
                    observeMessages(activeId)
                }
            }
        }
    }

    private fun observeMessages(convId: Long) {
        viewModelScope.launch {
            chatDao.getMessagesForConversation(convId).collect { msgList ->
                _uiState.value = _uiState.value.copy(messages = msgList)
            }
        }
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        val convId = _uiState.value.currentConversationId
        viewModelScope.launch {
            // Save user message
            chatDao.insertMessage(
                ChatMessageEntity(
                    conversationId = convId,
                    role = "user",
                    content = userText
                )
            )

            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                errorMessage = null,
                isOnline = repo.isOnline()
            )

            // Prepare history
            val history = _uiState.value.messages.takeLast(6).map { it.role to it.content }

            // Fetch AI response
            val result = repo.sendMessage(userText, history)

            // Save AI reply
            val replyMsgId = chatDao.insertMessage(
                ChatMessageEntity(
                    conversationId = convId,
                    role = "model",
                    content = result.text,
                    actionTag = result.actionTag
                )
            )

            _uiState.value = _uiState.value.copy(
                isProcessing = false,
                isOnline = result.isOnlineAi,
                errorMessage = result.error
            )

            // Execute action if present
            if (result.actionTag != null) {
                executeActionTag(result.actionTag)
            }

            // Speak if TTS enabled
            val currentSettings = settings.value
            if (currentSettings.ttsEnabled) {
                voiceManager?.speak(
                    text = result.text,
                    languageCode = currentSettings.language,
                    speed = currentSettings.voiceSpeed
                )
            }
        }
    }

    fun executeActionTag(tag: String) {
        val result: ActionResult = when {
            tag == "ACTION:FLASHLIGHT_ON" -> phoneManager.toggleTorch(true)
            tag == "ACTION:FLASHLIGHT_OFF" -> phoneManager.toggleTorch(false)
            tag == "ACTION:BATTERY" -> phoneManager.getBatteryStatus()
            tag == "ACTION:TIME" -> phoneManager.getCurrentDateTime()
            tag == "ACTION:VOLUME_UP" -> phoneManager.adjustVolume(1)
            tag == "ACTION:VOLUME_DOWN" -> phoneManager.adjustVolume(-1)
            tag.startsWith("ACTION:OPEN_APP:") -> {
                val appName = tag.removePrefix("ACTION:OPEN_APP:")
                phoneManager.openApp(appName)
            }
            tag == "ACTION:NAV_BACK" -> phoneManager.performAccessibilityAction("back")
            tag == "ACTION:NAV_HOME" -> phoneManager.performAccessibilityAction("home")
            tag == "ACTION:SCROLL_DOWN" -> phoneManager.performAccessibilityAction("scroll_down")
            tag == "ACTION:SCROLL_UP" -> phoneManager.performAccessibilityAction("scroll_up")
            tag.startsWith("ACTION:CLICK:") -> {
                val target = tag.removePrefix("ACTION:CLICK:")
                phoneManager.performAccessibilityAction("click_text", target)
            }
            tag.startsWith("ACTION:CALC:") -> {
                val expr = tag.removePrefix("ACTION:CALC:")
                phoneManager.calculateMath(expr)
            }
            else -> ActionResult.Failure("Unrecognized action: $tag")
        }

        val msg = when (result) {
            is ActionResult.Success -> "✓ ${result.message}"
            is ActionResult.Failure -> "⚠ ${result.reason}"
        }
        _uiState.value = _uiState.value.copy(lastActionResult = msg)
    }

    fun startListening() {
        voiceManager?.startListening(settings.value.language)
    }

    fun stopListening() {
        voiceManager?.stopListening()
    }

    fun speak(text: String) {
        voiceManager?.speak(text, settings.value.language, settings.value.voiceSpeed)
    }

    fun stopSpeaking() {
        voiceManager?.stopSpeaking()
    }

    fun emergencyStop() {
        voiceManager?.emergencyStop()
        _uiState.value = _uiState.value.copy(
            isProcessing = false,
            voiceState = VoiceState.IDLE,
            audioAmplitude = 0f,
            lastActionResult = "Emergency Stop Triggered."
        )
    }

    fun toggleFloatingAssistant(context: Context) {
        val currentlyActive = FloatingYunaService.isOverlayRunning
        if (currentlyActive) {
            FloatingYunaService.stop(context)
            _uiState.value = _uiState.value.copy(isFloatingActive = false)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Overlay permission required to float Yuna over other apps."
                )
                return
            }
            FloatingYunaService.start(context)
            _uiState.value = _uiState.value.copy(isFloatingActive = true)
        }
    }

    fun createNewConversation(title: String = "New Conversation") {
        viewModelScope.launch {
            val newId = chatDao.insertConversation(ConversationEntity(title = title))
            _uiState.value = _uiState.value.copy(currentConversationId = newId)
            observeMessages(newId)
        }
    }

    fun selectConversation(id: Long) {
        _uiState.value = _uiState.value.copy(currentConversationId = id)
        observeMessages(id)
    }

    fun clearMessages() {
        viewModelScope.launch {
            chatDao.deleteMessagesForConversation(_uiState.value.currentConversationId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager?.destroy()
    }
}
