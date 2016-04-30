package engineer.carrot.warren.warren.handler

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import engineer.carrot.warren.kale.irc.message.IMessage
import engineer.carrot.warren.kale.irc.message.ircv3.CapAckMessage
import engineer.carrot.warren.kale.irc.message.ircv3.CapEndMessage
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.state.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CapAckHandlerTests {

    lateinit var handler: CapAckHandler
    lateinit var state: CapState
    lateinit var sink: IMessageSink

    @Before fun setUp() {
        val capLifecycleState = CapLifecycle.NEGOTIATING
        state = CapState(lifecycle = capLifecycleState, negotiate = setOf(), server = mapOf(), accepted = setOf(), rejected = setOf())
        sink = mock()

        handler = CapAckHandler(state, sink)
    }

    @Test fun test_handle_AddsAckedCapsToStateList() {
        state.negotiate = setOf("cap1", "cap2", "cap3")

        handler.handle(CapAckMessage(caps = listOf("cap1", "cap2")))

        assertEquals(setOf("cap1", "cap2"), state.accepted)
    }

    @Test fun test_handle_Negotiating_NoRemainingCaps_SendsCapEnd() {
        state.lifecycle = CapLifecycle.NEGOTIATING
        state.negotiate = setOf("cap1", "cap2")

        handler.handle(CapAckMessage(caps = listOf("cap1", "cap2")))

        verify(sink).write(CapEndMessage())
    }

    @Test fun test_handle_Negotiating_RemainingCaps_DoesNotSendCapEnd() {
        state.lifecycle = CapLifecycle.NEGOTIATING
        state.negotiate = setOf("cap1", "cap2", "cap3")

        handler.handle(CapAckMessage(caps = listOf("cap1", "cap2")))

        verify(sink, never()).write(any<IMessage>())
    }

    @Test fun test_handle_NotNegotiating_NoRemainingCaps_DoesNotSendCapEnd() {
        state.lifecycle = CapLifecycle.NEGOTIATED
        state.negotiate = setOf("cap1", "cap2")

        handler.handle(CapAckMessage(caps = listOf("cap1", "cap2")))

        verify(sink, never()).write(any<IMessage>())
    }

}