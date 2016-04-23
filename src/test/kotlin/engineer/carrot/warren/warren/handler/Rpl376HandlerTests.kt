package engineer.carrot.warren.warren.handler

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import engineer.carrot.warren.kale.irc.message.rfc1459.JoinMessage
import engineer.carrot.warren.kale.irc.message.rpl.Rpl376Message
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.state.ConnectionState
import engineer.carrot.warren.warren.state.LifecycleState
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals

class Rpl376HandlerTests {
    lateinit var handler: Rpl376Handler
    lateinit var mockSink: IMessageSink
    lateinit var connectionState: ConnectionState

    @Before fun setUp() {
        val lifecycleState = LifecycleState.CONNECTING
        connectionState = ConnectionState(server = "test.server", port = 6697, nickname = "test-nick", username = "test-nick", lifecycle = lifecycleState)

        mockSink = mock()
        handler = Rpl376Handler(mockSink, channelsToJoin = listOf("#channel1", "#channel2"), connectionState = connectionState)
    }

    @Test fun test_handle_SendsPongWithCorrectToken() {
        handler.handle(Rpl376Message(source = "test.source", target = "test-user", contents = "end of motd"))

        verify(mockSink).write(JoinMessage(channels = listOf("#channel1", "#channel2")))
    }

    @Test fun test_handle_UpdatesLifecycleStateToConnected() {
        handler.handle(Rpl376Message(source = "test.source", target = "test-user", contents = "end of motd"))

        assertEquals(LifecycleState.CONNECTED, connectionState.lifecycle)
    }
}