package engineer.carrot.warren.warren.handler

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import engineer.carrot.warren.kale.irc.message.IMessage
import engineer.carrot.warren.kale.irc.message.extension.cap.CapAckMessage
import engineer.carrot.warren.kale.irc.message.extension.cap.CapEndMessage
import engineer.carrot.warren.kale.irc.message.extension.sasl.AuthenticateMessage
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.extension.cap.CapLifecycle
import engineer.carrot.warren.warren.extension.cap.CapState
import engineer.carrot.warren.warren.extension.cap.ICapManager
import engineer.carrot.warren.warren.extension.sasl.SaslState
import engineer.carrot.warren.warren.state.AuthLifecycle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class CapAckHandlerTests {

    lateinit var handler: CapAckHandler
    lateinit var state: CapState
    lateinit var saslState: SaslState
    lateinit var mockSink: IMessageSink
    lateinit var mockCapManager: ICapManager

    @Before fun setUp() {
        val capLifecycleState = CapLifecycle.NEGOTIATING
        state = CapState(lifecycle = capLifecycleState, negotiate = setOf(), server = mapOf(), accepted = setOf(), rejected = setOf())
        saslState = SaslState(shouldAuth = false, lifecycle = AuthLifecycle.AUTH_FAILED, credentials = null)
        mockSink = mock()
        mockCapManager = mock()

        handler = CapAckHandler(state, saslState, mockSink, mockCapManager)
    }

    @Test fun test_handle_AddsAckedCapsToStateList() {
        state.negotiate = setOf("cap1", "cap2", "cap3")

        handler.handle(CapAckMessage(caps = listOf("cap1", "cap2")), mapOf())

        assertEquals(setOf("cap1", "cap2"), state.accepted)
    }

    @Test fun test_handle_Negotiating_TellsCapManagerRegistrationStateChanged() {
        state.lifecycle = CapLifecycle.NEGOTIATING

        handler.handle(CapAckMessage(caps = listOf("cap 1", "cap 2")), mapOf())

        verify(mockCapManager).onRegistrationStateChanged()
    }

    @Test fun test_handle_ACKedSasl_ShouldAuth_ChangesSaslLifecycleToAuthing() {
        saslState.shouldAuth = true

        handler.handle(CapAckMessage(caps = listOf("sasl")), mapOf())

        assertEquals(AuthLifecycle.AUTHING, saslState.lifecycle)
    }

    @Test fun test_handle_ACKedSasl_NoAuth_DoesNotWriteAuthenticateMessage() {
        saslState.shouldAuth = false

        handler.handle(CapAckMessage(caps = listOf("sasl")), mapOf())

        verify(mockSink, never()).write(any())
    }

    @Test fun test_handle_ACKedSasl_ShouldAuth_WriteAuthenticatePlainMessage() {
        state.negotiate = setOf("sasl")
        saslState.shouldAuth = true

        handler.handle(CapAckMessage(caps = listOf("sasl")), mapOf())

        verify(mockSink).write(AuthenticateMessage(payload = "PLAIN", isEmpty = false))
    }

    @Test fun test_handle_ServerACKedCapThatWeDidntNegotiate_DoesNotAcceptIt() {
        state.negotiate = setOf("cap1", "cap2")

        handler.handle(CapAckMessage(caps = listOf("cap3")), mapOf())

        assertFalse(state.accepted.contains("cap3"))
        verify(mockCapManager, never()).capEnabled("cap3")
    }

}