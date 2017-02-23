package chat.willow.warren.handler.rpl

import chat.willow.kale.irc.message.rfc1459.rpl.Rpl353Message
import chat.willow.kale.irc.message.utility.CaseMapping
import chat.willow.kale.irc.tag.TagStore
import chat.willow.warren.state.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class Rpl353HandlerTests {

    lateinit var handler: Rpl353Handler
    lateinit var channelsState: ChannelsState
    lateinit var userPrefixesState: UserPrefixesState
    val caseMappingState = CaseMappingState(mapping = CaseMapping.RFC1459)

    @Before fun setUp() {
        channelsState = emptyChannelsState(caseMappingState)
        userPrefixesState = UserPrefixesState(prefixesToModes = mapOf('@' to 'o', '+' to 'v'))
        handler = Rpl353Handler(channelsState.joined, userPrefixesState, caseMappingState)
    }

    @Test fun test_handle_WellFormed_AddsCorrectNicksToChannel() {
        channelsState.joined += ChannelState("#channel", users = generateUsersFromNicks(listOf(), mappingState = caseMappingState))

        handler.handle(Rpl353Message(source = "test.server", target = "test-nick", visibility = "=", channel = "#channel", names = listOf("@test-nick@somewhere", "+another-person", "someone-else!realname@somewhere_else")), TagStore())

        assertEquals(channelsStateWith(listOf(ChannelState(name = "#channel", users = generateUsersWithModes(("test-nick" to setOf('o')), ("another-person" to setOf('v')), ("someone-else" to setOf()), mappingState = caseMappingState))), caseMappingState), channelsState)
    }

    @Test fun test_handle_MalformedUserNick_ProcessesTheRestAnyway() {
        channelsState.joined += ChannelState("#channel", users = generateUsersFromNicks(listOf(), mappingState = caseMappingState))

        handler.handle(Rpl353Message(source = "test.server", target = "test-nick", visibility = "=", channel = "#channel", names = listOf("@", "+another-person", "someone-else")), TagStore())

        assertEquals(channelsStateWith(listOf(ChannelState(name = "#channel", users = generateUsersWithModes(("another-person" to setOf('v')), ("someone-else" to setOf()), mappingState = caseMappingState))), caseMappingState), channelsState)
    }

    @Test fun test_handle_NotInChannel_DoesNothing() {
        handler.handle(Rpl353Message(source = "test.server", target = "test-nick", visibility = "=", channel = "#channel", names = listOf("@test-nick", "+another-person", "someone-else")), TagStore())

        assertEquals(emptyChannelsState(caseMappingState), channelsState)
    }
}