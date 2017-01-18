package chat.willow.warren.event.internal

import com.nhaarman.mockito_kotlin.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.concurrent.BlockingQueue

class InternalWarrenEventsTests {

    private lateinit var sut: WarrenInternalEventQueue
    private lateinit var mockQueue: BlockingQueue<IWarrenInternalEvent>

    @Before fun setUp() {
        mockQueue = mock()

        sut = WarrenInternalEventQueue(queue = mockQueue)
    }

    @Test fun test_grab_TakesValidItemFromQueue() {
        val event = mock<IWarrenInternalEvent>()
        whenever(mockQueue.take()).thenReturn(event)

        val returnedEvent = sut.grab()

        assertEquals(event, returnedEvent)
    }

    @Test fun test_grab_WhenInterrupted_ReturnsNull() {
        whenever(mockQueue.take()).then { throw InterruptedException() }

        val returnedEvent = sut.grab()

        assertNull(returnedEvent)
    }

    @Test fun test_add_Event_TellsQueueToAddEvent() {
        val event = mock<IWarrenInternalEvent>()

        sut.add(event)

        verify(mockQueue).add(event)
    }

    @Test fun test_add_Closure_TellsQueueToAddEvent_ThatExecutesClosure() {
        val eventClosure: () -> Unit = mock()
        sut.add(eventClosure)
        val captor = argumentCaptor<IWarrenInternalEvent>()
        verify(mockQueue).add(captor.capture())

        captor.firstValue.execute()

        verify(eventClosure).invoke()
    }

    @Test fun test_clear_TellsQueueToClear() {
        sut.clear()

        verify(mockQueue).clear()
    }

}