package engineer.carrot.warren.warren.handler

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import engineer.carrot.warren.kale.irc.message.IMessage
import engineer.carrot.warren.kale.irc.message.extension.cap.CapAckMessage
import engineer.carrot.warren.kale.irc.message.extension.cap.CapEndMessage
import engineer.carrot.warren.kale.irc.message.extension.cap.CapNakMessage
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.extension.cap.CapLifecycle
import engineer.carrot.warren.warren.extension.cap.CapState
import engineer.carrot.warren.warren.extension.cap.ICapManager
import engineer.carrot.warren.warren.extension.sasl.SaslState
import engineer.carrot.warren.warren.state.AuthLifecycle
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CapNakHandlerTests {

    lateinit var handler: CapNakHandler
    lateinit var capState: CapState
    lateinit var saslState: SaslState
    lateinit var sink: IMessageSink
    lateinit var mockCapManager: ICapManager

    @Before fun setUp() {
        val capLifecycleState = CapLifecycle.NEGOTIATING
        capState = CapState(lifecycle = capLifecycleState, negotiate = setOf(), server = mapOf(), accepted = setOf(), rejected = setOf())
        saslState = SaslState(shouldAuth = false, lifecycle = AuthLifecycle.AUTH_FAILED, credentials = null)
        sink = mock()
        mockCapManager = mock()

        handler = CapNakHandler(capState, saslState, sink, mockCapManager)
    }

    @Test fun test_handle_AddsNakedCapsToStateList() {
        capState.negotiate = setOf("cap1", "cap2", "cap3")

        handler.handle(CapNakMessage(caps = listOf("cap1", "cap2")), mapOf())

        assertEquals(setOf("cap1", "cap2"), capState.rejected)
    }

    @Test fun test_handle_Negotiating_TellsCapManagerRegistrationStateChanged() {
        capState.lifecycle = CapLifecycle.NEGOTIATING

        handler.handle(CapNakMessage(caps = listOf("cap1", "cap2")), mapOf())

        verify(mockCapManager).onRegistrationStateChanged()
    }

}