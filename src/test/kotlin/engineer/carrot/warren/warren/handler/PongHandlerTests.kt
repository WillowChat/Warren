package engineer.carrot.warren.warren.handler

import com.nhaarman.mockito_kotlin.mock
import engineer.carrot.warren.kale.irc.message.rfc1459.PongMessage
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.state.CapLifecycle
import engineer.carrot.warren.warren.state.CapState
import engineer.carrot.warren.warren.state.ConnectionState
import engineer.carrot.warren.warren.state.LifecycleState
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PongHandlerTests {

    lateinit var handler: PongHandler
    lateinit var mockSink: IMessageSink
    lateinit var connectionState: ConnectionState

    @Before fun setUp() {
        mockSink = mock()
        val lifecycleState = LifecycleState.DISCONNECTED
        val capLifecycleState = CapLifecycle.NEGOTIATED
        val capState = CapState(lifecycle = capLifecycleState, negotiate = setOf(), server = mapOf(), accepted = setOf(), rejected = setOf())
        connectionState = ConnectionState(server = "test.server", port = 6697, nickname = "test-nick", username = "test-nick", lifecycle = lifecycleState, cap = capState)
        handler = PongHandler(mockSink, connectionState)
    }

    @Test fun test_handle_UpdatesLastPingPongTimeToNow() {
        val expectedTime = System.currentTimeMillis()
        val tolerance = 1000

        handler.handle(PongMessage(token = "TestToken"), mapOf())

        assertTrue(connectionState.lastPingOrPong > tolerance)
        assertTrue(connectionState.lastPingOrPong > expectedTime - tolerance)
        assertTrue(connectionState.lastPingOrPong < expectedTime + tolerance)
    }

}