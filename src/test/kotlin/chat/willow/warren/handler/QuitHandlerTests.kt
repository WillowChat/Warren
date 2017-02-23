package chat.willow.warren.handler

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import chat.willow.kale.irc.message.rfc1459.QuitMessage
import chat.willow.kale.irc.message.utility.CaseMapping
import chat.willow.kale.irc.prefix.Prefix
import chat.willow.kale.irc.tag.TagStore
import chat.willow.warren.event.ConnectionLifecycleEvent
import chat.willow.warren.event.IWarrenEvent
import chat.willow.warren.event.IWarrenEventDispatcher
import chat.willow.warren.state.*
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

        connectionState = ConnectionState(server = "test.server", port = 6697, nickname = "test-nick", user = "test-nick", lifecycle = lifecycleState)
        channelsState = emptyChannelsState(caseMappingState)
        mockEventDispatcher = mock()
        handler = QuitHandler(mockEventDispatcher, connectionState, channelsState.joined)
    }

    @Test fun test_handle_SourceIsNull_DoesNothing() {
        channelsState.joined += ChannelState(name = "#channel", users = generateUsersFromNicks(listOf("someone", "someone-else"), mappingState = caseMappingState))

        handler.handle(QuitMessage(), TagStore())

        val expectedChannelOneState = ChannelState(name = "#channel", users = generateUsersFromNicks(listOf("someone", "someone-else"), mappingState = caseMappingState))
        val expectedChannelsState = channelsStateWith(listOf(expectedChannelOneState), caseMappingState)

        assertEquals(channelsState, expectedChannelsState)
    }

    @Test fun test_handle_SourceIsSelf_ChangesConnectionStateToDisconnected() {
        channelsState.joined += ChannelState(name = "#channel", users = generateUsersFromNicks(listOf("test-nick", "someone-else"), mappingState = caseMappingState))
        channelsState.joined += ChannelState(name = "#channel2", users = generateUsersFromNicks(listOf("another-person", "someone-else"), mappingState = caseMappingState))

        handler.handle(QuitMessage(source = Prefix(nick = "test-nick")), TagStore())

        val expectedChannelOneState = ChannelState(name = "#channel", users = generateUsersFromNicks(listOf("test-nick", "someone-else"), mappingState = caseMappingState))
        val expectedChannelTwoState = ChannelState(name = "#channel2", users = generateUsersFromNicks(listOf("another-person", "someone-else"), mappingState = caseMappingState))
        val expectedChannelsState = channelsStateWith(listOf(expectedChannelOneState, expectedChannelTwoState), caseMappingState)

        assertEquals(channelsState, expectedChannelsState)
        assertEquals(connectionState.lifecycle, LifecycleState.DISCONNECTED)
    }

    @Test fun test_handle_SourceIsSelf_FiresDisconnectedEvent() {
        handler.handle(QuitMessage(source = Prefix(nick = "test-nick")), TagStore())

        verify(mockEventDispatcher).fire(ConnectionLifecycleEvent(lifecycle = LifecycleState.DISCONNECTED))
    }

    @Test fun test_handle_SourceIsOther_RemovesUserFromChannels() {
        channelsState.joined += ChannelState(name = "#channel", users = generateUsersFromNicks(listOf("test-nick", "someone-else"), mappingState = caseMappingState))
        channelsState.joined += ChannelState(name = "#channel2", users = generateUsersFromNicks(listOf("another-person", "someone-else"), mappingState = caseMappingState))

        handler.handle(QuitMessage(source = Prefix(nick = "someone-else")), TagStore())

        val expectedChannelOneState = ChannelState(name = "#channel", users = generateUsersFromNicks(listOf("test-nick"), mappingState = caseMappingState))
        val expectedChannelTwoState = ChannelState(name = "#channel2", users = generateUsersFromNicks(listOf("another-person"), mappingState = caseMappingState))
        val expectedChannelsState = channelsStateWith(listOf(expectedChannelOneState, expectedChannelTwoState), caseMappingState)

        assertEquals(channelsState, expectedChannelsState)
    }

    @Test fun test_handle_SourceIsOther_DoesNotFireDisconnectedEvent() {
        handler.handle(QuitMessage(source = Prefix(nick = "someone-else")), TagStore())

        verify(mockEventDispatcher, never()).fire(any<IWarrenEvent>())
    }

}