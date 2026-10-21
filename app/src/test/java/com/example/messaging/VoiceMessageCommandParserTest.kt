package com.example.messaging

import com.example.messaging.model.MessagingCommand
import com.example.messaging.model.MessagingExecutionStage
import com.example.messaging.model.MessagingPlatform
import com.example.messaging.model.MessagingSettings
import com.example.messaging.parser.VoiceMessageCommandParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceMessageCommandParserTest {

    @Test
    fun `test English WhatsApp command parsing`() {
        val input = "Send a message to Alex on WhatsApp saying I will be late by 10 minutes"
        val cmd = VoiceMessageCommandParser.parse(input)

        assertEquals(MessagingPlatform.WHATSAPP, cmd.platform)
        assertEquals("Alex", cmd.recipient)
        assertEquals("I will be late by 10 minutes", cmd.messageBody)
        assertFalse(cmd.isAmbiguousRecipient)
        assertFalse(cmd.isAmbiguousPlatform)
    }

    @Test
    fun `test English Telegram colon syntax parsing`() {
        val input = "Telegram Michael: Send the report please"
        val cmd = VoiceMessageCommandParser.parse(input)

        assertEquals(MessagingPlatform.TELEGRAM, cmd.platform)
        assertEquals("Michael", cmd.recipient)
        assertEquals("Send the report please", cmd.messageBody)
        assertFalse(cmd.isAmbiguousRecipient)
        assertFalse(cmd.isAmbiguousPlatform)
    }

    @Test
    fun `test English SMS text parsing`() {
        val input = "Text John: Where are you?"
        val cmd = VoiceMessageCommandParser.parse(input)

        assertEquals(MessagingPlatform.SMS, cmd.platform)
        assertEquals("John", cmd.recipient)
        assertEquals("Where are you?", cmd.messageBody)
        assertFalse(cmd.isAmbiguousRecipient)
    }

    @Test
    fun `test English Messenger parsing`() {
        val input = "Send message on Messenger to David: Hey what's up"
        val cmd = VoiceMessageCommandParser.parse(input)

        assertEquals(MessagingPlatform.MESSENGER, cmd.platform)
        assertEquals("David", cmd.recipient)
        assertEquals("Hey what's up", cmd.messageBody)
    }

    @Test
    fun `test Bengali WhatsApp command parsing`() {
        val input = "হোয়াটসঅ্যাপে রাহিমকে: আমি ক্লাসে আছি"
        val cmd = VoiceMessageCommandParser.parse(input)

        assertEquals(MessagingPlatform.WHATSAPP, cmd.platform)
        assertEquals("আমি ক্লাসে আছি", cmd.messageBody)
        assertFalse(cmd.isAmbiguousPlatform)
    }

    @Test
    fun `test Korean WhatsApp command parsing`() {
        val input = "왓츠앱으로 민수에게: 10분 뒤에 도착해"
        val cmd = VoiceMessageCommandParser.parse(input)

        assertEquals(MessagingPlatform.WHATSAPP, cmd.platform)
        assertEquals("10분 뒤에 도착해", cmd.messageBody)
        assertFalse(cmd.isAmbiguousPlatform)
    }

    @Test
    fun `test recipient ambiguity with multiple candidates`() {
        val input = "Send message on WhatsApp to Alex or David: Meeting at 3"
        val cmd = VoiceMessageCommandParser.parse(input)

        assertEquals(MessagingPlatform.WHATSAPP, cmd.platform)
        assertTrue(cmd.isAmbiguousRecipient)
        assertTrue(cmd.candidateRecipients.contains("Alex"))
        assertTrue(cmd.candidateRecipients.contains("David"))
    }

    @Test
    fun `test missing recipient ambiguity`() {
        val input = "Send a message on WhatsApp: Hello"
        val cmd = VoiceMessageCommandParser.parse(input)

        assertEquals(MessagingPlatform.WHATSAPP, cmd.platform)
        assertTrue(cmd.isAmbiguousRecipient || cmd.recipient.isNullOrBlank())
    }

    @Test
    fun `test unspecified destination platform ambiguity`() {
        val input = "Send a message to Alex: Hello there"
        val cmd = VoiceMessageCommandParser.parse(input)

        assertEquals(MessagingPlatform.UNKNOWN, cmd.platform)
        assertTrue(cmd.isAmbiguousPlatform)
        assertEquals("Alex", cmd.recipient)
        assertEquals("Hello there", cmd.messageBody)
    }

    @Test
    fun `test execution stages enum integrity`() {
        // Assert that stages are distinct and defined
        val stages = MessagingExecutionStage.values().toList()
        assertTrue(stages.contains(MessagingExecutionStage.APP_OPENED))
        assertTrue(stages.contains(MessagingExecutionStage.DRAFT_PREPARED))
        assertTrue(stages.contains(MessagingExecutionStage.ACTION_ATTEMPTED))
        assertTrue(stages.contains(MessagingExecutionStage.DELIVERY_CONFIRMED))
        assertTrue(stages.contains(MessagingExecutionStage.FAILED))
    }
}
