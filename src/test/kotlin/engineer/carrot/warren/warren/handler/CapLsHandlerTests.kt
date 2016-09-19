package engineer.carrot.warren.warren.handler

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import engineer.carrot.warren.kale.irc.message.IMessage
import engineer.carrot.warren.kale.irc.message.ircv3.CapEndMessage
import engineer.carrot.warren.kale.irc.message.ircv3.CapLsMessage
import engineer.carrot.warren.kale.irc.message.ircv3.CapReqMessage
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.extension.cap.CapLifecycle
import engineer.carrot.warren.warren.extension.cap.CapState
import engineer.carrot.warren.warren.extension.sasl.SaslState
import engineer.carrot.warren.warren.state.AuthLifecycle
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CapLsHandlerTests {

    lateinit var handler: CapLsHandler
    lateinit var capState: CapState
    lateinit var saslState: SaslState
    lateinit var sink: IMessageSink

    @Before fun setUp() {
        val capLifecycleState = CapLifecycle.NEGOTIATING
        capState = CapState(lifecycle = capLifecycleState, negotiate = setOf(), server = mapOf(), accepted = setOf(), rejected = setOf())
        saslState = SaslState(shouldAuth = false, lifecycle = AuthLifecycle.AUTH_FAILED, credentials = null)
        sink = mock()

        handler = CapLsHandler(capState, saslState, sink)
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

    @Test fun test_handle_Negotiating_ImplicitlyRejectsMissingCaps_NoneLeft_SendsCapEnd() {
        capState.lifecycle = CapLifecycle.NEGOTIATING
        capState.negotiate = setOf("cap1", "cap2")

        handler.handle(CapLsMessage(caps = mapOf()), mapOf())

        verify(sink).write(CapEndMessage())
    }

    @Test fun test_handle_Negotiating_ImplicitlyRejectsMissingCaps_SomeLeft_DoesNotSendCapEnd() {
        capState.lifecycle = CapLifecycle.NEGOTIATING
        capState.negotiate = setOf("cap1", "cap2", "cap3")

        handler.handle(CapLsMessage(caps = mapOf("cap1" to null, "cap2" to null)), mapOf())

        verify(sink, never()).write(CapEndMessage())
    }

    @Test fun test_handle_Negotiating_SendsCapReqForSupportedCaps() {
        capState.lifecycle = CapLifecycle.NEGOTIATING
        capState.negotiate = setOf("cap1", "cap2")

        handler.handle(CapLsMessage(caps = mapOf("cap1" to null, "cap2" to null)), mapOf())

        verify(sink).write(CapReqMessage(caps = listOf("cap1", "cap2")))
    }

    @Test fun test_handle_Negotiating_MultilineLs_DoesNothingElse() {
        capState.lifecycle = CapLifecycle.NEGOTIATING
        capState.negotiate = setOf("cap1", "cap2")

        handler.handle(CapLsMessage(caps = mapOf("cap1" to null, "cap2" to null), isMultiline = true), mapOf())

        verify(sink, never()).write(any<IMessage>())
    }

    @Test fun test_handle_NotNegotiating_DoesNothingElse() {
        capState.lifecycle = CapLifecycle.NEGOTIATED
        capState.negotiate = setOf("cap1", "cap2")

        handler.handle(CapLsMessage(caps = mapOf("cap1" to null, "cap2" to null)), mapOf())

        verify(sink, never()).write(any<IMessage>())
    }

}