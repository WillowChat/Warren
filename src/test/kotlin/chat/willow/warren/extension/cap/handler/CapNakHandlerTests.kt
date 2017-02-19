package chat.willow.warren.extension.cap.handler

import chat.willow.kale.irc.message.extension.cap.CapNakMessage
import chat.willow.kale.irc.tag.TagStore
import chat.willow.warren.IMessageSink
import chat.willow.warren.extension.cap.CapLifecycle
import chat.willow.warren.extension.cap.CapState
import chat.willow.warren.extension.cap.ICapManager
import chat.willow.warren.extension.sasl.SaslState
import chat.willow.warren.state.AuthLifecycle
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
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

        handler.handle(CapNakMessage(caps = listOf("cap1", "cap2")), TagStore())

        assertEquals(setOf("cap1", "cap2"), capState.rejected)
    }

    @Test fun test_handle_Negotiating_TellsCapManagerRegistrationStateChanged() {
        capState.lifecycle = CapLifecycle.NEGOTIATING

        handler.handle(CapNakMessage(caps = listOf("cap1", "cap2")), TagStore())

        verify(mockCapManager).onRegistrationStateChanged()
    }

}