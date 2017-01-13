package engineer.carrot.warren.warren.event.internal

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import engineer.carrot.warren.kale.IKale
import engineer.carrot.warren.warren.ILineSource
import engineer.carrot.warren.warren.event.IWarrenEventDispatcher
import engineer.carrot.warren.warren.event.RawIncomingLineEvent
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

class NewLineWarrenEventGeneratorTests {

    private lateinit var sut: NewLineWarrenEventGenerator
    private lateinit var mockQueue: IWarrenInternalEventQueue
    private lateinit var mockKale: IKale
    private lateinit var mockLineSource: ILineSource
    private lateinit var mockWarrenEventDispatcher: IWarrenEventDispatcher

    @Before fun setUp() {
        mockQueue = mock()
        mockKale = mock()
        mockLineSource = mock()
        mockWarrenEventDispatcher = mock()

        sut = NewLineWarrenEventGenerator(mockQueue, mockKale, mockLineSource, true, mockWarrenEventDispatcher)
    }

    @Test fun test_run_LineAvailable_AddsNewLineEventToQueue() {
        val line = ""
        whenever(mockLineSource.nextLine()).thenReturn(line).thenReturn(null)

        sut.run()

        verify(mockQueue).add(NewLineEvent(line, mockKale))
    }

    @Test fun test_run_LineNotAvailable_DoesNothingWithQueue() {
        whenever(mockLineSource.nextLine()).thenReturn(null)

        sut.run()

        verify(mockQueue, never()).add(any<IWarrenInternalEvent>())
        verify(mockQueue, never()).add(any<() -> Unit>())
    }

    @Test fun test_run_FireIncomingLineEvent_EventDispatcherFiresCorrectEvent() {
        val line = "test line"
        whenever(mockLineSource.nextLine()).thenReturn(line).thenReturn(null)

        sut.run()

        verify(mockWarrenEventDispatcher).fire(RawIncomingLineEvent(line))
    }

    @Test fun test_run_DontFireIncomingLineEvent_EventDispatcherFiresCorrectEvent() {
        sut = NewLineWarrenEventGenerator(mockQueue, mockKale, mockLineSource, false, mockWarrenEventDispatcher)
        val line = "test line"
        whenever(mockLineSource.nextLine()).thenReturn(line).thenReturn(null)

        sut.run()

        verify(mockWarrenEventDispatcher, never()).fire(RawIncomingLineEvent(line))
    }

}