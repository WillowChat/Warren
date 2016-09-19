package engineer.carrot.warren.warren.handler.rpl

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import engineer.carrot.warren.kale.irc.message.rfc1459.JoinMessage
import engineer.carrot.warren.kale.irc.message.rpl.Rpl376Message
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.event.ConnectionLifecycleEvent
import engineer.carrot.warren.warren.event.IWarrenEventDispatcher
import engineer.carrot.warren.warren.extension.cap.CapLifecycle
import engineer.carrot.warren.warren.extension.cap.CapState
import engineer.carrot.warren.warren.state.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class Rpl376HandlerTests {
    lateinit var handler: Rpl376Handler
    lateinit var mockSink: IMessageSink
    lateinit var connectionState: ConnectionState
    lateinit var capState: CapState
    lateinit var mockEventDispatcher: IWarrenEventDispatcher

    @Before fun setUp() {
        val lifecycleState = LifecycleState.CONNECTING
        val capLifecycleState = CapLifecycle.NEGOTIATED
        capState = CapState(lifecycle = capLifecycleState, negotiate = setOf(), server = mapOf(), accepted = setOf(), rejected = setOf())
        connectionState = ConnectionState(server = "test.server", port = 6697, nickname = "test-nick", user = "test-nick", lifecycle = lifecycleState)

        mockSink = mock()
        mockEventDispatcher = mock()
        handler = Rpl376Handler(mockEventDispatcher, mockSink, channelsToJoin = mapOf("#channel1" to null, "#channel2" to "testkey"), connectionState = connectionState, capState = capState)
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

    @Test fun test_handle_ShouldIdentifyWithNickServ_NoCredentials_SetsAuthLifecycleToFailed() {
        connectionState.nickServ = NickServState(shouldAuth = true, lifecycle = AuthLifecycle.AUTHING, credentials = null, channelJoinWaitSeconds = 0)

        handler.handle(Rpl376Message(source = "test.source", target = "test-user", contents = "end of motd"), mapOf())

        assertEquals(AuthLifecycle.AUTH_FAILED, connectionState.nickServ.lifecycle)
    }

    @Test fun test_handle_ShouldIdentifyWithNickServ_WithCredentials_SetsAuthLifecycleAuthed() {
        val credentials = AuthCredentials(account = "", password = "")
        connectionState.nickServ = NickServState(shouldAuth = true, lifecycle = AuthLifecycle.AUTHING, credentials = credentials, channelJoinWaitSeconds = 0)

        handler.handle(Rpl376Message(source = "test.source", target = "test-user", contents = "end of motd"), mapOf())

        assertEquals(AuthLifecycle.AUTHED, connectionState.nickServ.lifecycle)
    }

    @Test fun test_handle_ShouldIdentifyWithNickServ_WithCredentials_SendsNickservIdentify() {
        val credentials = AuthCredentials(account = "account", password = "password")
        connectionState.nickServ = NickServState(shouldAuth = true, lifecycle = AuthLifecycle.AUTHING, credentials = credentials, channelJoinWaitSeconds = 0)

        handler.handle(Rpl376Message(source = "test.source", target = "test-user", contents = "end of motd"), mapOf())

        verify(mockSink).writeRaw("NICKSERV identify account password")
    }

    @Test fun test_handle_ShouldNotIdentifyWithNickserv_SendsNoRawMessages() {
        val handler = Rpl376Handler(mockEventDispatcher, mockSink, channelsToJoin = mapOf(), connectionState = connectionState, capState = capState)

        handler.handle(Rpl376Message(source = "test.source", target = "test-user", contents = "end of motd"), mapOf())

        verify(mockSink, never()).writeRaw(any())
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
        capState.lifecycle = CapLifecycle.NEGOTIATING

        handler.handle(Rpl376Message(source = "test.source", target = "test-user", contents = "end of motd"), mapOf())

        assertEquals(CapLifecycle.FAILED, capState.lifecycle)
    }
}