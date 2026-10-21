package com.example.messaging.model

enum class MessagingPlatform(val displayName: String, val packageName: String?) {
    WHATSAPP("WhatsApp", "com.whatsapp"),
    TELEGRAM("Telegram", "org.telegram.messenger"),
    SMS("SMS (Default Messenger)", null),
    MESSENGER("Facebook Messenger", "com.facebook.orca"),
    UNKNOWN("Unknown / Unspecified", null)
}

enum class MessagingExecutionStage(val label: String) {
    APP_OPENED("App Opened"),
    DRAFT_PREPARED("Message Draft Prepared"),
    ACTION_ATTEMPTED("Send Action Attempted"),
    DELIVERY_CONFIRMED("Sending Confirmed by System"),
    FAILED("Action Failed or Cancelled")
}

data class MessagingCommand(
    val platform: MessagingPlatform,
    val recipient: String?,
    val messageBody: String?,
    val isAmbiguousRecipient: Boolean = false,
    val isAmbiguousPlatform: Boolean = false,
    val candidateRecipients: List<String> = emptyList(),
    val rawInput: String
)

data class MessagingActionResult(
    val stage: MessagingExecutionStage,
    val platform: MessagingPlatform,
    val summary: String,
    val details: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class MessagingSettings(
    val isMasterEnabled: Boolean = true,
    val isWhatsAppEnabled: Boolean = true,
    val isTelegramEnabled: Boolean = true,
    val isSmsEnabled: Boolean = true,
    val isMessengerEnabled: Boolean = true,
    val isDirectSmsSendEnabled: Boolean = false,
    val requireConfirmationBeforeSend: Boolean = true
)

data class MessagingLogItem(
    val id: Long = System.currentTimeMillis(),
    val platform: MessagingPlatform,
    val recipientMasked: String,
    val stage: MessagingExecutionStage,
    val summary: String,
    val timestamp: Long = System.currentTimeMillis()
)
