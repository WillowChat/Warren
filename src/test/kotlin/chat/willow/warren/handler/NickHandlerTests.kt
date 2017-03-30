package chat.willow.warren.handler

import chat.willow.kale.helper.CaseMapping
import chat.willow.kale.irc.message.rfc1459.NickMessage
import chat.willow.kale.irc.prefix.Prefix
import chat.willow.kale.irc.tag.TagStore
import chat.willow.warren.state.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class NickHandlerTests {

    lateinit var handler: NickHandler
    lateinit var connectionState: ConnectionState
    lateinit var channelsState: ChannelsState
    val caseMappingState = CaseMappingState(mapping = CaseMapping.RFC1459)

    @Before fun setUp() {
        val lifecycleState = LifecycleState.DISCONNECTED

        connectionState = ConnectionState(server = "test.server", port = 6697, nickname = "test-nick", user = "test-nick", lifecycle = lifecycleState)
        channelsState = emptyChannelsState(caseMappingState)
        handler = NickHandler(connectionState, channelsState.joined, caseMappingState)
    }

    @Test fun test_handle_FromIsSelf_RenamesSelf() {
        channelsState.joined += ChannelState(name = "#channel", users = generateUsersFromNicks(listOf("test-nick", "someone-else"), mappingState = caseMappingState))
        channelsState.joined += ChannelState(name = "#channel2", users = generateUsersFromNicks(listOf("another-person", "someone-else"), mappingState = caseMappingState))

        handler.handle(NickMessage.Message(source = Prefix(nick = "test-nick"), nickname = "test-new-nick"), TagStore())

        val expectedChannelOneState = ChannelState(name = "#channel", users = generateUsersFromNicks(listOf("test-new-nick", "someone-else"), mappingState = caseMappingState))
        val expectedChannelTwoState = ChannelState(name = "#channel2", users = generateUsersFromNicks(listOf("another-person", "someone-else"), mappingState = caseMappingState))
        val expectedChannelsState = channelsStateWith(listOf(expectedChannelOneState, expectedChannelTwoState), caseMappingState)

        assertEquals(channelsState, expectedChannelsState)
    }

    @Test fun test_handle_FromIsSelf_DifferingCase_RenamesSelf() {
        channelsState.joined += ChannelState(name = "#channel", users = generateUsersFromNicks(listOf("test-nick", "someone-else"), mappingState = caseMappingState))
        channelsState.joined += ChannelState(name = "#channel2", users = generateUsersFromNicks(listOf("another-person", "someone-else"), mappingState = caseMappingState))

        handler.handle(NickMessage.Message(source = Prefix(nick = "Test-Nick"), nickname = "test-new-nick"), TagStore())

        val expectedChannelOneState = ChannelState(name = "#channel", users = generateUsersFromNicks(listOf("test-new-nick", "someone-else"), mappingState = caseMappingState))
        val expectedChannelTwoState = ChannelState(name = "#channel2", users = generateUsersFromNicks(listOf("another-person", "someone-else"), mappingState = caseMappingState))
        val expectedChannelsState = channelsStateWith(listOf(expectedChannelOneState, expectedChannelTwoState), caseMappingState)

        assertEquals(channelsState, expectedChannelsState)
    }

    @Test fun test_handle_UserChangesCaseInName_RetainsModes() {
        channelsState.joined += ChannelState(name = "#channel", users = generateUsersWithModes(("me" to setOf('o', 'v')), mappingState = caseMappingState))

        handler.handle(NickMessage.Message(source = Prefix(nick = "me"), nickname = "me-new"), TagStore())

        val expectedChannelOneState = ChannelState(name = "#channel", users = generateUsersWithModes(("me-new" to setOf('o', 'v')), mappingState = caseMappingState))
        val expectedChannelsState = channelsStateWith(listOf(expectedChannelOneState), caseMappingState)

        assertEquals(channelsState, expectedChannelsState)
    }

    @Test fun test_handle_FromIsSomeoneElse_UserIsRenamedInAllChannels() {
        channelsState.joined += ChannelState(name = "#channel", users = generateUsersFromNicks(listOf("someone", "someone-else"), mappingState = caseMappingState))
        channelsState.joined += ChannelState(name = "#channel2", users = generateUsersFromNicks(listOf("another-person", "someone-else"), mappingState = caseMappingState))

        handler.handle(NickMessage.Message(source = Prefix(nick = "someone-else"), nickname = "someone-else-2"), TagStore())

        val expectedChannelOneState = ChannelState(name = "#channel", users = generateUsersFromNicks(listOf("someone", "someone-else-2"), mappingState = caseMappingState))
        val expectedChannelTwoState = ChannelState(name = "#channel2", users = generateUsersFromNicks(listOf("another-person", "someone-else-2"), mappingState = caseMappingState))
        val expectedChannelsState = channelsStateWith(listOf(expectedChannelOneState, expectedChannelTwoState), caseMappingState)

        assertEquals(channelsState, expectedChannelsState)
    }

}