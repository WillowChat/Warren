package engineer.carrot.warren.warren.handler.rpl

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import engineer.carrot.warren.kale.irc.message.rfc1459.JoinMessage
import engineer.carrot.warren.kale.irc.message.rpl.Rpl376Message
import engineer.carrot.warren.warren.ConnectionLifecycleEvent
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.IWarrenEventDispatcher
import engineer.carrot.warren.warren.state.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class Rpl376HandlerTests {
    lateinit var handler: Rpl376Handler
    lateinit var mockSink: IMessageSink
    lateinit var connectionState: ConnectionState
    lateinit var mockEventDispatcher: IWarrenEventDispatcher

    @Before fun setUp() {
        val lifecycleState = LifecycleState.CONNECTING
        val capLifecycleState = CapLifecycle.NEGOTIATED
        val capState = CapState(lifecycle = capLifecycleState, negotiate = setOf(), server = mapOf(), accepted = setOf(), rejected = setOf())
        val saslState = SaslState(shouldAuth = false, lifecycle = SaslLifecycle.AUTH_FAILED, credentials = null)
        connectionState = ConnectionState(server = "test.server", port = 6697, nickname = "test-nick", username = "test-nick", lifecycle = lifecycleState, cap = capState, sasl = saslState)

        mockSink = mock()
        mockEventDispatcher = mock()
        handler = Rpl376Handler(mockEventDispatcher, mockSink, channelsToJoin = mapOf("#channel1" to null, "#channel2" to "testkey"), connectionState = connectionState)
    }

    @Test fun test_handle_JoinsChannelsAfterGettingMOTD_ConnectingState() {
        connectionState.lifecycle = LifecycleState.CONNECTING

        handler.handle(Rpl376Message(source = "test.source", target = "test-user", contents = "end of motd"), mapOf())

        verify(mockSink).write(JoinMessage(channels = listOf("#channel1")))
        verify(mockSink).write(JoinMessage(channels = listOf("#channel2"), keys = listOf("testkey")))
    }

    @Test fun test_handle_JoinsChannelsAfterGettingMOTD_RegisteringState() {
        connectionState.lifecycle = LifecycleState.REGISTERING

        handler.handle(Rpl376Message(source = "test.source", target = "test-user", contents = "end of motd"), mapOf())

        verify(mockSink).write(JoinMessage(channels = listOf("#channel1")))
        verify(mockSink).write(JoinMessage(channels = listOf("#channel2"), keys = listOf("testkey")))
    }

    @Test fun test_handle_UpdatesLifecycleStateToConnected() {
        handler.handle(Rpl376Message(source = "test.source", target = "test-user", contents = "end of motd"), mapOf())

        assertEquals(LifecycleState.CONNECTED, connectionState.lifecycle)
    }

    @Test fun test_handle_FiresConnectedEvent() {
        handler.handle(Rpl376Message(source = "test.source", target = "test-user", contents = "end of motd"), mapOf())

        verify(mockEventDispatcher).fire(ConnectionLifecycleEvent(lifecycle = LifecycleState.CONNECTED))
    }

    @Test fun test_handle_CapNegotiating_SetsToFailed() {
        connectionState.cap.lifecycle = CapLifecycle.NEGOTIATING

        handler.handle(Rpl376Message(source = "test.source", target = "test-user", contents = "end of motd"), mapOf())

        assertEquals(CapLifecycle.FAILED, connectionState.cap.lifecycle)
    }
}