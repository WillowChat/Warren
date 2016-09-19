package engineer.carrot.warren.warren.handler

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import engineer.carrot.warren.kale.irc.message.IMessage
import engineer.carrot.warren.kale.irc.message.ircv3.CapAckMessage
import engineer.carrot.warren.kale.irc.message.ircv3.CapEndMessage
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.extension.cap.CapLifecycle
import engineer.carrot.warren.warren.extension.cap.CapState
import engineer.carrot.warren.warren.extension.cap.ICapManager
import engineer.carrot.warren.warren.extension.sasl.SaslState
import engineer.carrot.warren.warren.state.AuthLifecycle
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CapAckHandlerTests {

    lateinit var handler: CapAckHandler
    lateinit var state: CapState
    lateinit var saslState: SaslState
    lateinit var sink: IMessageSink
    lateinit var capManager: ICapManager

    @Before fun setUp() {
        val capLifecycleState = CapLifecycle.NEGOTIATING
        state = CapState(lifecycle = capLifecycleState, negotiate = setOf(), server = mapOf(), accepted = setOf(), rejected = setOf())
        saslState = SaslState(shouldAuth = false, lifecycle = AuthLifecycle.AUTH_FAILED, credentials = null)
        sink = mock()
        capManager = mock()

        handler = CapAckHandler(state, saslState, sink, capManager)
    }

    @Test fun test_handle_AddsAckedCapsToStateList() {
        state.negotiate = setOf("cap1", "cap2", "cap3")

        handler.handle(CapAckMessage(caps = listOf("cap1", "cap2")), mapOf())

        assertEquals(setOf("cap1", "cap2"), state.accepted)
    }

    @Test fun test_handle_Negotiating_NoRemainingCaps_SendsCapEnd() {
        state.lifecycle = CapLifecycle.NEGOTIATING
        state.negotiate = setOf("cap1", "cap2")

        handler.handle(CapAckMessage(caps = listOf("cap1", "cap2")), mapOf())

        verify(sink).write(CapEndMessage())
    }

    @Test fun test_handle_Negotiating_RemainingCaps_DoesNotSendCapEnd() {
        state.lifecycle = CapLifecycle.NEGOTIATING
        state.negotiate = setOf("cap1", "cap2", "cap3")

        handler.handle(CapAckMessage(caps = listOf("cap1", "cap2")), mapOf())

        verify(sink, never()).write(any<IMessage>())
    }

    @Test fun test_handle_NotNegotiating_NoRemainingCaps_DoesNotSendCapEnd() {
        state.lifecycle = CapLifecycle.NEGOTIATED
        state.negotiate = setOf("cap1", "cap2")

        handler.handle(CapAckMessage(caps = listOf("cap1", "cap2")), mapOf())

        verify(sink, never()).write(any<IMessage>())
    }

}