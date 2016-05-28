package engineer.carrot.warren.warren.handler

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import engineer.carrot.warren.kale.irc.message.rfc1459.QuitMessage
import engineer.carrot.warren.kale.irc.message.utility.CaseMapping
import engineer.carrot.warren.kale.irc.prefix.Prefix
import engineer.carrot.warren.warren.ConnectionLifecycleEvent
import engineer.carrot.warren.warren.IWarrenEvent
import engineer.carrot.warren.warren.IWarrenEventDispatcher
import engineer.carrot.warren.warren.state.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class QuitHandlerTests {

    lateinit var handler: QuitHandler
    lateinit var connectionState: ConnectionState
    lateinit var channelsState: ChannelsState
    lateinit var mockEventDispatcher: IWarrenEventDispatcher
    val caseMappingState = CaseMappingState(mapping = CaseMapping.RFC1459)

    @Before fun setUp() {
        val lifecycleState = LifecycleState.CONNECTED
        val capLifecycleState = CapLifecycle.NEGOTIATED
        val capState = CapState(lifecycle = capLifecycleState, negotiate = setOf(), server = mapOf(), accepted = setOf(), rejected = setOf())
        val saslState = SaslState(shouldAuth = false, lifecycle = SaslLifecycle.AUTH_FAILED, credentials = null)
        connectionState = ConnectionState(server = "test.server", port = 6697, nickname = "test-nick", username = "test-nick", lifecycle = lifecycleState, cap = capState, sasl = saslState)
        channelsState = emptyChannelsState(caseMappingState)
        mockEventDispatcher = mock()
        handler = QuitHandler(mockEventDispatcher, connectionState, channelsState.joined)
    }

    @Test fun test_handle_SourceIsNull_DoesNothing() {
        channelsState.joined += ChannelState(name = "#channel", users = generateUsers("someone", "someone-else", mappingState = caseMappingState))

        handler.handle(QuitMessage(), mapOf())

        val expectedChannelOneState = ChannelState(name = "#channel", users = generateUsers("someone", "someone-else", mappingState = caseMappingState))
        val expectedChannelsState = channelsStateWith(listOf(expectedChannelOneState), caseMappingState)

        assertEquals(channelsState, expectedChannelsState)
    }

    @Test fun test_handle_SourceIsSelf_ChangesConnectionStateToDisconnected() {
        channelsState.joined += ChannelState(name = "#channel", users = generateUsers("test-nick", "someone-else", mappingState = caseMappingState))
        channelsState.joined += ChannelState(name = "#channel2", users = generateUsers("another-person", "someone-else", mappingState = caseMappingState))

        handler.handle(QuitMessage(source = Prefix(nick = "test-nick")), mapOf())

        val expectedChannelOneState = ChannelState(name = "#channel", users = generateUsers("test-nick", "someone-else", mappingState = caseMappingState))
        val expectedChannelTwoState = ChannelState(name = "#channel2", users = generateUsers("another-person", "someone-else", mappingState = caseMappingState))
        val expectedChannelsState = channelsStateWith(listOf(expectedChannelOneState, expectedChannelTwoState), caseMappingState)

        assertEquals(channelsState, expectedChannelsState)
        assertEquals(connectionState.lifecycle, LifecycleState.DISCONNECTED)
    }

    @Test fun test_handle_SourceIsSelf_FiresDisconnectedEvent() {
        handler.handle(QuitMessage(source = Prefix(nick = "test-nick")), mapOf())

        verify(mockEventDispatcher).fire(ConnectionLifecycleEvent(lifecycle = LifecycleState.DISCONNECTED))
    }

    @Test fun test_handle_SourceIsOther_RemovesUserFromChannels() {
        channelsState.joined += ChannelState(name = "#channel", users = generateUsers("test-nick", "someone-else", mappingState = caseMappingState))
        channelsState.joined += ChannelState(name = "#channel2", users = generateUsers("another-person", "someone-else", mappingState = caseMappingState))

        handler.handle(QuitMessage(source = Prefix(nick = "someone-else")), mapOf())

        val expectedChannelOneState = ChannelState(name = "#channel", users = generateUsers("test-nick", mappingState = caseMappingState))
        val expectedChannelTwoState = ChannelState(name = "#channel2", users = generateUsers("another-person", mappingState = caseMappingState))
        val expectedChannelsState = channelsStateWith(listOf(expectedChannelOneState, expectedChannelTwoState), caseMappingState)

        assertEquals(channelsState, expectedChannelsState)
    }

    @Test fun test_handle_SourceIsOther_DoesNotFireDisconnectedEvent() {
        handler.handle(QuitMessage(source = Prefix(nick = "someone-else")), mapOf())

        verify(mockEventDispatcher, never()).fire(any<IWarrenEvent>())
    }

}