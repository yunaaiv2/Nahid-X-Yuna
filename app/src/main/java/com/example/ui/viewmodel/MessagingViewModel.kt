package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.YunaApplication
import com.example.messaging.dispatcher.MessageDispatcher
import com.example.messaging.model.MessagingActionResult
import com.example.messaging.model.MessagingCommand
import com.example.messaging.model.MessagingLogItem
import com.example.messaging.model.MessagingPlatform
import com.example.messaging.model.MessagingSettings
import com.example.messaging.parser.VoiceMessageCommandParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MessagingViewModel : ViewModel() {

    private val app = YunaApplication.instance
    val dispatcher = MessageDispatcher(app.applicationContext)

    private val _settings = MutableStateFlow(MessagingSettings())
    val settings: StateFlow<MessagingSettings> = _settings.asStateFlow()

    private val _pendingCommand = MutableStateFlow<MessagingCommand?>(null)
    val pendingCommand: StateFlow<MessagingCommand?> = _pendingCommand.asStateFlow()

    private val _isConfirmPreviewVisible = MutableStateFlow(false)
    val isConfirmPreviewVisible: StateFlow<Boolean> = _isConfirmPreviewVisible.asStateFlow()

    private val _isAmbiguityVisible = MutableStateFlow(false)
    val isAmbiguityVisible: StateFlow<Boolean> = _isAmbiguityVisible.asStateFlow()

    private val _lastResult = MutableStateFlow<MessagingActionResult?>(null)
    val lastResult: StateFlow<MessagingActionResult?> = _lastResult.asStateFlow()

    val actionLogs: StateFlow<List<MessagingLogItem>> = dispatcher.actionLogs

    private val _isDispatching = MutableStateFlow(false)
    val isDispatching: StateFlow<Boolean> = _isDispatching.asStateFlow()

    fun handleVoiceInput(voiceText: String) {
        val parsed = VoiceMessageCommandParser.parse(voiceText)
        _pendingCommand.value = parsed

        if (parsed.isAmbiguousRecipient || parsed.isAmbiguousPlatform) {
            _isAmbiguityVisible.value = true
            _isConfirmPreviewVisible.value = false
        } else {
            _isAmbiguityVisible.value = false
            _isConfirmPreviewVisible.value = true
        }
    }

    fun selectClarifiedRecipient(recipient: String) {
        val current = _pendingCommand.value ?: return
        val updated = current.copy(
            recipient = recipient,
            isAmbiguousRecipient = false
        )
        _pendingCommand.value = updated
        checkIfReadyToPreview(updated)
    }

    fun selectClarifiedPlatform(platform: MessagingPlatform) {
        val current = _pendingCommand.value ?: return
        val updated = current.copy(
            platform = platform,
            isAmbiguousPlatform = false
        )
        _pendingCommand.value = updated
        checkIfReadyToPreview(updated)
    }

    fun updateMessageBody(newBody: String) {
        val current = _pendingCommand.value ?: return
        _pendingCommand.value = current.copy(messageBody = newBody)
    }

    private fun checkIfReadyToPreview(cmd: MessagingCommand) {
        if (!cmd.isAmbiguousRecipient && !cmd.isAmbiguousPlatform && !cmd.recipient.isNullOrBlank()) {
            _isAmbiguityVisible.value = false
            _isConfirmPreviewVisible.value = true
        }
    }

    fun executeConfirmedDispatch(useAccessibility: Boolean = false) {
        val cmd = _pendingCommand.value ?: return
        _isDispatching.value = true
        viewModelScope.launch {
            val result = dispatcher.dispatch(cmd, _settings.value, useAccessibility)
            _lastResult.value = result
            _isDispatching.value = false
            _isConfirmPreviewVisible.value = false
            _pendingCommand.value = null
        }
    }

    fun cancelPending() {
        _pendingCommand.value = null
        _isConfirmPreviewVisible.value = false
        _isAmbiguityVisible.value = false
    }

    fun emergencyStop() {
        dispatcher.triggerEmergencyHalt()
        cancelPending()
        _lastResult.value = null
    }

    fun resetHalt() {
        dispatcher.resetHalt()
    }

    fun toggleMaster(enabled: Boolean) {
        _settings.value = _settings.value.copy(isMasterEnabled = enabled)
    }

    fun togglePlatform(platform: MessagingPlatform, enabled: Boolean) {
        val s = _settings.value
        _settings.value = when (platform) {
            MessagingPlatform.WHATSAPP -> s.copy(isWhatsAppEnabled = enabled)
            MessagingPlatform.TELEGRAM -> s.copy(isTelegramEnabled = enabled)
            MessagingPlatform.SMS -> s.copy(isSmsEnabled = enabled)
            MessagingPlatform.MESSENGER -> s.copy(isMessengerEnabled = enabled)
            MessagingPlatform.UNKNOWN -> s
        }
    }

    fun toggleDirectSms(enabled: Boolean) {
        _settings.value = _settings.value.copy(isDirectSmsSendEnabled = enabled)
    }

    fun toggleRequireConfirmation(enabled: Boolean) {
        _settings.value = _settings.value.copy(requireConfirmationBeforeSend = enabled)
    }
}
