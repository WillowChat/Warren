package chat.willow.warren.handler

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import chat.willow.kale.irc.message.IMessage
import chat.willow.kale.irc.message.extension.cap.CapAckMessage
import chat.willow.kale.irc.message.extension.cap.CapEndMessage
import chat.willow.kale.irc.message.extension.cap.CapLsMessage
import chat.willow.kale.irc.message.extension.cap.CapReqMessage
import chat.willow.kale.irc.message.extension.sasl.Rpl903Message
import chat.willow.warren.IMessageSink
import chat.willow.warren.extension.cap.CapLifecycle
import chat.willow.warren.extension.cap.CapState
import chat.willow.warren.extension.cap.ICapManager
import chat.willow.warren.extension.cap.handler.CapLsHandler
import chat.willow.warren.extension.sasl.SaslState
import chat.willow.warren.state.AuthLifecycle
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CapLsHandlerTests {

    lateinit var handler: CapLsHandler
    lateinit var capState: CapState
    lateinit var saslState: SaslState
    lateinit var mockSink: IMessageSink
    lateinit var mockCapManager: ICapManager

    @Before fun setUp() {
        val capLifecycleState = CapLifecycle.NEGOTIATING
        capState = CapState(lifecycle = capLifecycleState, negotiate = setOf(), server = mapOf(), accepted = setOf(), rejected = setOf())
        saslState = SaslState(shouldAuth = false, lifecycle = AuthLifecycle.AUTH_FAILED, credentials = null)
        mockSink = mock()
        mockCapManager = mock()

        handler = CapLsHandler(capState, saslState, mockSink, mockCapManager)
    }

    @Test fun test_handle_AddsCapsToStateList() {
        capState.negotiate = setOf("cap1", "cap2", "cap3")

        handler.handle(CapLsMessage(caps = mapOf("cap1" to null, "cap2" to "value")), mapOf())

        assertEquals(mapOf("cap1" to null, "cap2" to "value"), capState.server)
    }

    @Test fun test_handle_Negotiating_ImplicitlyRejectsMissingCaps() {
        capState.lifecycle = CapLifecycle.NEGOTIATING
        capState.negotiate = setOf("cap1", "cap2", "cap3", "cap4")

        handler.handle(CapLsMessage(caps = mapOf("cap1" to null, "cap2" to null)), mapOf())

        assertEquals(setOf("cap3", "cap4"), capState.rejected)
    }

    @Test fun test_handle_Negotiating_TellsCapManagerRegistrationStateChanged() {
        capState.lifecycle = CapLifecycle.NEGOTIATING

        handler.handle(CapLsMessage(caps = mapOf("cap1" to null, "cap2" to null)), mapOf())

        verify(mockCapManager).onRegistrationStateChanged()
    }

    @Test fun test_handle_Negotiating_SendsCapReqForSupportedCaps() {
        capState.lifecycle = CapLifecycle.NEGOTIATING
        capState.negotiate = setOf("cap1", "cap2")

        handler.handle(CapLsMessage(caps = mapOf("cap1" to null, "cap2" to null)), mapOf())

        verify(mockSink).write(CapReqMessage(caps = listOf("cap1", "cap2")))
    }

    @Test fun test_handle_Negotiating_MultilineLs_DoesNothingElse() {
        capState.lifecycle = CapLifecycle.NEGOTIATING
        capState.negotiate = setOf("cap1", "cap2")

        handler.handle(CapLsMessage(caps = mapOf("cap1" to null, "cap2" to null), isMultiline = true), mapOf())

        verify(mockSink, never()).write(any<IMessage>())
    }

    @Test fun test_handle_NotNegotiating_DoesNothingElse() {
        capState.lifecycle = CapLifecycle.NEGOTIATED
        capState.negotiate = setOf("cap1", "cap2")

        handler.handle(CapLsMessage(caps = mapOf("cap1" to null, "cap2" to null)), mapOf())

        verify(mockSink, never()).write(any<IMessage>())
    }

}