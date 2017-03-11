package chat.willow.warren.handler

import chat.willow.kale.irc.message.rfc1459.KickMessage
import chat.willow.kale.irc.message.utility.CaseMapping
import chat.willow.kale.irc.tag.TagStore
import chat.willow.warren.state.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class KickHandlerTests {

    lateinit var handler: KickHandler
    lateinit var connectionState: ConnectionState
    lateinit var channelsState: ChannelsState
    val caseMappingState = CaseMappingState(mapping = CaseMapping.RFC1459)

    @Before fun setUp() {
        val lifecycleState = LifecycleState.DISCONNECTED

        connectionState = ConnectionState(server = "test.server", port = 6697, nickname = "test-nick", user = "test-nick", lifecycle = lifecycleState)
        channelsState = emptyChannelsState(caseMappingState)
        handler = KickHandler(connectionState, channelsState.joined, caseMappingState)
    }

    @Test fun test_handle_SingleNick_MultipleUsers_RemovesUserFromChannels() {
        channelsState.joined += ChannelState(name = "#channel", users = generateUsersFromNicks(listOf("someone", "someone-else"), mappingState = caseMappingState))
        channelsState.joined += ChannelState(name = "#channel2", users = generateUsersFromNicks(listOf("another-person", "someone-else"), mappingState = caseMappingState))

        handler.handle(KickMessage(users = listOf("someone"), channels = listOf("#channel", "#channel2")), TagStore())

        val expectedChannelOneState = ChannelState(name = "#channel", users = generateUsersFromNicks(listOf("someone-else"), mappingState = caseMappingState))
        val expectedChannelTwoState = ChannelState(name = "#channel2", users = generateUsersFromNicks(listOf("another-person", "someone-else"), mappingState = caseMappingState))
        val expectedChannelsState = channelsStateWith(listOf(expectedChannelOneState, expectedChannelTwoState), caseMappingState)

        assertEquals(channelsState, expectedChannelsState)
    }

    @Test fun test_handle_MultipleNicks_NotSelf_RemovesFromChannel() {
        channelsState.joined += ChannelState(name = "#channel", users = generateUsersFromNicks(listOf("someone", "someone-else", "last-person"), mappingState = caseMappingState))

        handler.handle(KickMessage(users = listOf("someone", "someone-else"), channels = listOf("#channel")), TagStore())

        val expectedChannelOneState = ChannelState(name = "#channel", users = generateUsersFromNicks(listOf("last-person"), mappingState = caseMappingState))
        val expectedChannelsState = channelsStateWith(listOf(expectedChannelOneState), caseMappingState)

        assertEquals(channelsState, expectedChannelsState)
    }

    @Test fun test_handle_UserNotInChannel_DoesNothing() {
        channelsState.joined += ChannelState(name = "#channel", users = generateUsersFromNicks(listOf("someone"), mappingState = caseMappingState))

        handler.handle(KickMessage(users = listOf("nonexistent-user"), channels = listOf("#channel")), TagStore())

        val expectedChannelOneState = ChannelState(name = "#channel", users = generateUsersFromNicks(listOf("someone"), mappingState = caseMappingState))
        val expectedChannelsState = channelsStateWith(listOf(expectedChannelOneState), caseMappingState)

        assertEquals(channelsState, expectedChannelsState)
    }

    @Test fun test_handle_KickSelf_LeavesChannel() {
        channelsState.joined += ChannelState(name = "#channel", users = generateUsersFromNicks(listOf("test-nick"), mappingState = caseMappingState))

        handler.handle(KickMessage(users = listOf("test-nick"), channels = listOf("#channel")), TagStore())

        val expectedChannelsState = emptyChannelsState(caseMappingState)

        assertEquals(channelsState, expectedChannelsState)
    }

    @Test fun test_handle_KickSelf_DifferingCase_LeavesChannel() {
        channelsState.joined += ChannelState(name = "#channel", users = generateUsersFromNicks(listOf("test-nick"), mappingState = caseMappingState))

        handler.handle(KickMessage(users = listOf("Test-Nick"), channels = listOf("#Channel")), TagStore())

        val expectedChannelsState = emptyChannelsState(caseMappingState)

        assertEquals(channelsState, expectedChannelsState)
    }

}