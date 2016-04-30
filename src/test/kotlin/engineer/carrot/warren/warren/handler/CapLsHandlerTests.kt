package engineer.carrot.warren.warren.handler

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import engineer.carrot.warren.kale.irc.message.IMessage
import engineer.carrot.warren.kale.irc.message.ircv3.CapLsMessage
import engineer.carrot.warren.kale.irc.message.ircv3.CapEndMessage
import engineer.carrot.warren.kale.irc.message.ircv3.CapReqMessage
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.state.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CapLsHandlerTests {

    lateinit var handler: CapLsHandler
    lateinit var state: CapState
    lateinit var sink: IMessageSink

    @Before fun setUp() {
        val capLifecycleState = CapLifecycle.NEGOTIATING
        state = CapState(lifecycle = capLifecycleState, negotiate = setOf(), server = mapOf(), accepted = setOf(), rejected = setOf())
        sink = mock()

        handler = CapLsHandler(state, sink)
    }

    @Test fun test_handle_AddsCapsToStateList() {
        state.negotiate = setOf("cap1", "cap2", "cap3")

        handler.handle(CapLsMessage(caps = mapOf("cap1" to null, "cap2" to "value")))

        assertEquals(mapOf("cap1" to null, "cap2" to "value"), state.server)
    }

    @Test fun test_handle_Negotiating_ImplicitlyRejectsMissingCaps() {
        state.lifecycle = CapLifecycle.NEGOTIATING
        state.negotiate = setOf("cap1", "cap2", "cap3", "cap4")

        handler.handle(CapLsMessage(caps = mapOf("cap1" to null, "cap2" to null)))

        assertEquals(setOf("cap3", "cap4"), state.rejected)
    }

    @Test fun test_handle_Negotiating_ImplicitlyRejectsMissingCaps_NoneLeft_SendsCapEnd() {
        state.lifecycle = CapLifecycle.NEGOTIATING
        state.negotiate = setOf("cap1", "cap2")

        handler.handle(CapLsMessage(caps = mapOf()))

        verify(sink).write(CapEndMessage())
    }

    @Test fun test_handle_Negotiating_ImplicitlyRejectsMissingCaps_SomeLeft_DoesNotSendCapEnd() {
        state.lifecycle = CapLifecycle.NEGOTIATING
        state.negotiate = setOf("cap1", "cap2", "cap3")

        handler.handle(CapLsMessage(caps = mapOf("cap1" to null, "cap2" to null)))

        verify(sink, never()).write(CapEndMessage())
    }

    @Test fun test_handle_Negotiating_SendsCapReqForSupportedCaps() {
        state.lifecycle = CapLifecycle.NEGOTIATING
        state.negotiate = setOf("cap1", "cap2")

        handler.handle(CapLsMessage(caps = mapOf("cap1" to null, "cap2" to null)))

        verify(sink).write(CapReqMessage(caps = listOf("cap1")))
        verify(sink).write(CapReqMessage(caps = listOf("cap2")))
    }

    @Test fun test_handle_Negotiating_MultilineLs_DoesNothingElse() {
        state.lifecycle = CapLifecycle.NEGOTIATING
        state.negotiate = setOf("cap1", "cap2")

        handler.handle(CapLsMessage(caps = mapOf("cap1" to null, "cap2" to null), isMultiline = true))

        verify(sink, never()).write(any<IMessage>())
    }

    @Test fun test_handle_NotNegotiating_DoesNothingElse() {
        state.lifecycle = CapLifecycle.NEGOTIATED
        state.negotiate = setOf("cap1", "cap2")

        handler.handle(CapLsMessage(caps = mapOf("cap1" to null, "cap2" to null)))

        verify(sink, never()).write(any<IMessage>())
    }

}