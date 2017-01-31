package chat.willow.warren.extension.cap.handler

import chat.willow.kale.irc.message.extension.cap.CapNewMessage
import chat.willow.kale.irc.message.extension.cap.CapReqMessage
import chat.willow.warren.IMessageSink
import chat.willow.warren.extension.cap.CapLifecycle
import chat.willow.warren.extension.cap.CapState
import chat.willow.warren.extension.cap.handler.CapNewHandler
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test

class CapNewHandlerTests {

    lateinit var handler: CapNewHandler
    lateinit var state: CapState
    lateinit var mockSink: IMessageSink

    @Before fun setUp() {
        val capLifecycleState = CapLifecycle.NEGOTIATING
        state = CapState(lifecycle = capLifecycleState, negotiate = setOf(), server = mapOf(), accepted = setOf(), rejected = setOf())
        mockSink = mock()

        handler = CapNewHandler(state, mockSink)
    }

    @Test fun test_handle_CapsToNegotiate_NoneAccepted_ReqsCaps() {
        state.negotiate = setOf("cap1", "cap2")
        state.accepted = setOf()

        handler.handle(CapNewMessage(target = "", caps = mapOf("cap1" to null, "cap2" to null, "cap3" to null)), mapOf())

        verify(mockSink).write(CapReqMessage(caps = listOf("cap1", "cap2")))
    }

    @Test fun test_handle_CapsToNegotiate_SomeAlreadyAccepted_ReqsCorrectCaps() {
        state.negotiate = setOf("cap1", "cap2")
        state.accepted = setOf("cap1")

        handler.handle(CapNewMessage(target = "", caps = mapOf("cap1" to null, "cap2" to null, "cap3" to null)), mapOf())

        verify(mockSink).write(CapReqMessage(caps = listOf("cap2")))
    }

    @Test fun test_handle_CapsToNegotiate_AllAlreadyAccepted_ReqsNothing() {
        state.negotiate = setOf("cap1", "cap2")
        state.accepted = setOf("cap1", "cap2")

        handler.handle(CapNewMessage(target = "", caps = mapOf("cap1" to null, "cap2" to null, "cap3" to null)), mapOf())

        verify(mockSink, never()).write(any())
    }

}