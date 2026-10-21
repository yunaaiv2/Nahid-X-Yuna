package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.messaging.dispatcher.MessageDispatcher
import com.example.messaging.model.MessagingCommand
import com.example.messaging.model.MessagingExecutionStage
import com.example.messaging.model.MessagingPlatform
import com.example.messaging.model.MessagingSettings
import com.example.phone.PhoneActionManager
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("YUNA AI", appName)
    }

    @Test
    fun `test phone action math calculation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = PhoneActionManager(context)
        val result = manager.calculateMath("25 * 4")
        assertTrue(result is com.example.phone.ActionResult.Success)
        assertTrue((result as com.example.phone.ActionResult.Success).message.contains("100"))
    }

    @Test
    fun `test result reporting never claims delivery confirmed for intent draft`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dispatcher = MessageDispatcher(context)
        val cmd = MessagingCommand(
            platform = MessagingPlatform.WHATSAPP,
            recipient = "1234567890",
            messageBody = "Hello world",
            rawInput = "test"
        )
        val settings = MessagingSettings(isMasterEnabled = true, isWhatsAppEnabled = true)
        val result = dispatcher.dispatch(cmd, settings, useAccessibilityIfAvailable = false)

        assertNotEquals(MessagingExecutionStage.DELIVERY_CONFIRMED, result.stage)
        assertTrue(
            result.stage == MessagingExecutionStage.DRAFT_PREPARED ||
                    result.stage == MessagingExecutionStage.APP_OPENED ||
                    result.stage == MessagingExecutionStage.FAILED
        )
    }

    @Test
    fun `test emergency halt immediately blocks messaging actions`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dispatcher = MessageDispatcher(context)
        dispatcher.triggerEmergencyHalt()

        val cmd = MessagingCommand(
            platform = MessagingPlatform.SMS,
            recipient = "1234567890",
            messageBody = "Important alert",
            rawInput = "test"
        )
        val result = dispatcher.dispatch(cmd, MessagingSettings())

        assertEquals(MessagingExecutionStage.FAILED, result.stage)
        assertTrue(result.summary.contains("Emergency Stop"))
    }
}
