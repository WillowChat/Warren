package chat.willow.warren.handler

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import chat.willow.kale.irc.message.rfc1459.PingMessage
import chat.willow.kale.irc.message.rfc1459.PongMessage
import chat.willow.warren.IMessageSink
import chat.willow.warren.state.ConnectionState
import chat.willow.warren.state.LifecycleState
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PingHandlerTests {

    lateinit var handler: PingHandler
    lateinit var mockSink: IMessageSink
    lateinit var connectionState: ConnectionState

    @Before fun setUp() {
        mockSink = mock()
        val lifecycleState = LifecycleState.DISCONNECTED

        connectionState = ConnectionState(server = "test.server", port = 6697, nickname = "test-nick", user = "test-nick", lifecycle = lifecycleState)
        handler = PingHandler(mockSink, connectionState)
    }

    @Test fun test_handle_SendsPongWithCorrectToken() {
        handler.handle(PingMessage(token = "TestToken"), mapOf())

        verify(mockSink).write(PongMessage(token = "TestToken"))
    }

    @Test fun test_handle_UpdatesLastPingPongTimeToNow() {
        val expectedTime = System.currentTimeMillis()
        val tolerance = 1000

        handler.handle(PingMessage(token = "TestToken"), mapOf())

        assertTrue(connectionState.lastPingOrPong > tolerance)
        assertTrue(connectionState.lastPingOrPong > expectedTime - tolerance)
        assertTrue(connectionState.lastPingOrPong < expectedTime + tolerance)
    }

}