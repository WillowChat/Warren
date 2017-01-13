package engineer.carrot.warren.warren.event

import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test

class WarrenEventDispatcherTests {

    private lateinit var sut: WarrenEventDispatcher

    @Before fun setUp() {
        sut = WarrenEventDispatcher()
    }

    @Test fun test_fire_WithCorrectListenerRegistered_SendsEventToListener() {
        val event = mock<IWarrenEvent>()
        val listener: (IWarrenEvent) -> Unit = mock()
        sut.on(event::class, listener)

        sut.fire(event)

        verify(listener).invoke(event)
    }

    @Test fun test_fire_WithoutCorrectListenerRegistered_DoesNotSendEventToListener() {
        val firstEvent = object : IWarrenEvent { }
        val secondEvent = object : IWarrenEvent { }
        val listener: (IWarrenEvent) -> Unit = mock()
        sut.on(secondEvent::class, listener)

        sut.fire(firstEvent)

        verify(listener, never()).invoke(firstEvent)
        verify(listener, never()).invoke(secondEvent)
    }

    @Test fun test_onAnything_FiresListenersForAnyEvent() {
        val firstEvent = object : IWarrenEvent { }
        val secondEvent = object : IWarrenEvent { }
        val listener: (Any) -> Unit = mock()
        sut.onAnything(listener)

        sut.fire(firstEvent)
        sut.fire(secondEvent)

        inOrder(listener) {
            verify(listener).invoke(firstEvent)
            verify(listener).invoke(secondEvent)
        }
    }

}