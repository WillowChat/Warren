package chat.willow.warren.extension.cap.handler

import chat.willow.kale.irc.message.extension.cap.CapDelMessage
import chat.willow.kale.irc.tag.TagStore
import chat.willow.warren.IMessageSink
import chat.willow.warren.extension.cap.CapLifecycle
import chat.willow.warren.extension.cap.CapState
import chat.willow.warren.extension.cap.ICapManager
import chat.willow.warren.extension.cap.handler.CapDelHandler
import com.nhaarman.mockito_kotlin.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CapDelHandlerTests {

    lateinit var handler: CapDelHandler
    lateinit var capState: CapState
    lateinit var mockSink: IMessageSink
    lateinit var mockCapManager: ICapManager

    @Before fun setUp() {
        val capLifecycleState = CapLifecycle.NEGOTIATING
        capState = CapState(lifecycle = capLifecycleState, negotiate = setOf(), server = mapOf(), accepted = setOf(), rejected = setOf())
        mockSink = mock()
        mockCapManager = mock()

        handler = CapDelHandler(capState, mockSink, mockCapManager)
    }

    @Test fun test_handle_CapsDeleted_NoneRejected_NoneAccepted_DoesNothing() {
        capState.rejected = setOf()
        capState.accepted = setOf()

        handler.handle(CapDelMessage(target = "", caps = listOf("cap1", "cap2")), TagStore())

        verify(mockSink, never()).write(any())
    }

    @Test fun test_handle_CapsDeleted_SomeRejected_NoneAccepted_DoesNothing() {
        capState.rejected = setOf("cap1", "cap2")
        capState.accepted = setOf()

        handler.handle(CapDelMessage(target = "", caps = listOf("cap1", "cap2")), TagStore())

        verify(mockSink, never()).write(any())
    }

    @Test fun test_handle_CapsDeleted_SomeRejected_SomeAccepted_DisablesCorrectCaps() {
        capState.rejected = setOf("cap1")
        capState.accepted = setOf("cap2", "cap3")

        handler.handle(CapDelMessage(target = "", caps = listOf("cap1", "cap2", "cap3")), TagStore())

        assertEquals(setOf("cap1", "cap2", "cap3"), capState.rejected)
        assertEquals(setOf<String>(), capState.accepted)

        inOrder(mockCapManager) {
            verify(mockCapManager).capDisabled("cap2")
            verify(mockCapManager).capDisabled("cap3")
        }
    }

}