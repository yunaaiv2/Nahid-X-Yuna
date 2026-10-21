package com.example.messaging.parser

import com.example.messaging.model.MessagingCommand
import com.example.messaging.model.MessagingPlatform

object VoiceMessageCommandParser {

    fun parse(rawText: String): MessagingCommand {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty()) {
            return MessagingCommand(
                platform = MessagingPlatform.UNKNOWN,
                recipient = null,
                messageBody = null,
                isAmbiguousRecipient = true,
                isAmbiguousPlatform = true,
                rawInput = rawText
            )
        }

        val platform = detectPlatform(trimmed)
        val isAmbiguousPlatform = (platform == MessagingPlatform.UNKNOWN)

        val (recipient, message) = extractRecipientAndMessage(trimmed, platform)

        val isAmbiguousRecipient = recipient.isNullOrBlank() || recipient.contains(" or ", ignoreCase = true)
        val candidates = if (recipient != null && recipient.contains(" or ", ignoreCase = true)) {
            recipient.split(" or ", " অথবা ", " 아니면 ").map { it.trim() }.filter { it.isNotEmpty() }
        } else {
            emptyList()
        }

        return MessagingCommand(
            platform = platform,
            recipient = recipient?.trim(),
            messageBody = message?.trim(),
            isAmbiguousRecipient = isAmbiguousRecipient,
            isAmbiguousPlatform = isAmbiguousPlatform,
            candidateRecipients = candidates,
            rawInput = rawText
        )
    }

    private fun detectPlatform(text: String): MessagingPlatform {
        val lower = text.lowercase()
        return when {
            lower.contains("whatsapp") || lower.contains("হোয়াটসঅ্যাপ") || lower.contains("왓츠앱") -> MessagingPlatform.WHATSAPP
            lower.contains("telegram") || lower.contains("টেলিগ্রাম") || lower.contains("텔레그램") -> MessagingPlatform.TELEGRAM
            lower.contains("messenger") || lower.contains("ফেসবুক মেসেঞ্জার") || lower.contains("মেসেঞ্জার") || lower.contains("메신저") -> MessagingPlatform.MESSENGER
            lower.contains("sms") || lower.contains("text ") || lower.contains("টেক্সট") || lower.contains("এসএমএস") || lower.contains("문자") -> MessagingPlatform.SMS
            else -> MessagingPlatform.UNKNOWN
        }
    }

    private fun extractRecipientAndMessage(text: String, platform: MessagingPlatform): Pair<String?, String?> {
        // Strategy 1: Check colon separator (e.g. "Text John: I am here", "হোয়াটসঅ্যাপে রাহিমকে: আসছি")
        if (text.contains(":")) {
            val parts = text.split(":", limit = 2)
            val header = parts[0].trim()
            val message = parts[1].trim()
            val recipient = extractRecipientFromHeader(header)
            return Pair(recipient, message)
        }

        // Strategy 2: English "saying" / "that" / "message"
        val sayingRegex = Regex("""(?:to|to\s+contact)\s+([^,:\n]+?)\s+(?:on\s+\w+\s+)?(?:saying|that|with\s+message)\s+(.+)""", RegexOption.IGNORE_CASE)
        val matchSaying = sayingRegex.find(text)
        if (matchSaying != null) {
            val recipient = cleanRecipient(matchSaying.groupValues[1])
            val message = matchSaying.groupValues[2].trim()
            return Pair(recipient, message)
        }

        // Strategy 3: "Text <Name> <Message>"
        val textRegex = Regex("""(?:text|message|sms)\s+([A-Za-z0-9+]+)\s+(.+)""", RegexOption.IGNORE_CASE)
        val matchText = textRegex.find(text)
        if (matchText != null) {
            val recipient = cleanRecipient(matchText.groupValues[1])
            val message = matchText.groupValues[2].trim()
            return Pair(recipient, message)
        }

        // Strategy 4: Bengali parsing (e.g. "রাহিমকে মেসেজ পাঠাও আমি ক্লাসে আছি" or "আব্বুকে বলো আসছি")
        val bnRegex = Regex("""(?:হোয়াটসঅ্যাপে|টেলিগ্রামে|মেসেঞ্জারে|এসএমএসে)?\s*([^\s]+?)(?:কে|রে)?\s*(?:মেসেজ|বার্তা)?\s*(?:পাঠাও|বলো|করো)\s*(.+)""")
        val matchBn = bnRegex.find(text)
        if (matchBn != null) {
            val recipient = cleanRecipient(matchBn.groupValues[1])
            val message = matchBn.groupValues[2].trim()
            if (message.isNotEmpty()) {
                return Pair(recipient, message)
            }
        }

        // Strategy 5: Korean parsing (e.g. "민수에게 메시지 보내 10분 뒤 도착" or "엄마한테 문자 보내 지금 가")
        val koRegex = Regex("""(?:왓츠앱으로|텔레그램으로|메신저로)?\s*([^\s]+?)(?:에게|한테)\s*(?:메시지|문자)?\s*(?:보내|전송)\s*(.+)""")
        val matchKo = koRegex.find(text)
        if (matchKo != null) {
            val recipient = cleanRecipient(matchKo.groupValues[1])
            val message = matchKo.groupValues[2].trim()
            if (message.isNotEmpty()) {
                return Pair(recipient, message)
            }
        }

        // Fallback: Check if recipient is mentioned after "to "
        val toIndex = text.indexOf("to ", ignoreCase = true)
        if (toIndex != -1) {
            val afterTo = text.substring(toIndex + 3).trim()
            val tokens = afterTo.split(" ", limit = 2)
            val recipient = cleanRecipient(tokens[0])
            val message = tokens.getOrNull(1)?.trim()
            return Pair(recipient, message)
        }

        return Pair(null, null)
    }

    private fun extractRecipientFromHeader(header: String): String? {
        val cleaned = header
            .replace(Regex("""(?i)\b(send|a|message|text|on|via|whatsapp|telegram|messenger|sms|to)\b"""), " ")
            .replace("হোয়াটসঅ্যাপে", " ")
            .replace("টেলিগ্রামে", " ")
            .replace("মেসেঞ্জারে", " ")
            .replace("এসএমএস", " ")
            .replace("পাঠাও", " ")
            .replace("বলো", " ")
            .replace("করো", " ")
            .replace("কে", " ")
            .replace("왓츠앱으로", " ")
            .replace("텔레그램으로", " ")
            .replace("메신저로", " ")
            .replace("에게", " ")
            .replace("한테", " ")
            .replace("문자", " ")
            .replace("보내", " ")
            .trim()
        return if (cleaned.isNotEmpty()) cleaned else null
    }

    private fun cleanRecipient(raw: String): String {
        return raw.trim()
            .replace(Regex("""^(on\s+whatsapp|on\s+telegram|on\s+messenger)\s+""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s+(on\s+whatsapp|on\s+telegram|on\s+messenger)$""", RegexOption.IGNORE_CASE), "")
            .trim()
    }
}
