package engineer.carrot.warren.warren.extension.sasl

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import engineer.carrot.warren.kale.irc.message.IMessage
import engineer.carrot.warren.kale.irc.message.extension.cap.CapEndMessage
import engineer.carrot.warren.kale.irc.message.extension.sasl.Rpl904Message
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.extension.cap.CapLifecycle
import engineer.carrot.warren.warren.extension.cap.CapState
import engineer.carrot.warren.warren.extension.cap.ICapManager
import engineer.carrot.warren.warren.state.AuthLifecycle
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class Rpl904HandlerTests {

    lateinit var handler: Rpl904Handler
    lateinit var capState: CapState
    lateinit var saslState: SaslState
    lateinit var mockSink: IMessageSink
    lateinit var mockCapManager: ICapManager

    @Before fun setUp() {
        val capLifecycleState = CapLifecycle.NEGOTIATING
        capState = CapState(lifecycle = capLifecycleState, negotiate = setOf(), server = mapOf(), accepted = setOf(), rejected = setOf())
        saslState = SaslState(shouldAuth = false, lifecycle = AuthLifecycle.AUTHING, credentials = null)
        mockSink = mock()
        mockCapManager = mock()

        handler = Rpl904Handler(mockCapManager, saslState, mockSink)
    }

    @Test fun test_handle_LifecycleSetToAuthFailed() {
        handler.handle(Rpl904Message(source = "", target = "", contents = "SASL auth succeeded"), mapOf())

        assertEquals(AuthLifecycle.AUTH_FAILED, saslState.lifecycle)
    }

    @Test fun test_handle_RemainingCaps_DoesNotEndNegotiation() {
        capState.lifecycle = CapLifecycle.NEGOTIATING
        capState.negotiate = setOf("cap1", "cap2")
        capState.accepted = setOf("cap1")

        handler.handle(Rpl904Message(source = "", target = "", contents = "SASL auth succeeded"), mapOf())

        verify(mockSink, never()).write(any<IMessage>())
    }

    @Test fun test_handle_NoRemainingCaps_EndsNegotiation() {
        capState.lifecycle = CapLifecycle.NEGOTIATING
        capState.negotiate = setOf("cap1", "cap2")
        capState.accepted = setOf("cap1", "cap2")

        handler.handle(Rpl904Message(source = "", target = "", contents = "SASL auth succeeded"), mapOf())

        verify(mockSink).write(CapEndMessage())
    }

}