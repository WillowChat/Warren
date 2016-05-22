package engineer.carrot.warren.warren.handler.rpl

import engineer.carrot.warren.kale.irc.message.rpl.Rpl353Message
import engineer.carrot.warren.kale.irc.message.utility.CaseMapping
import engineer.carrot.warren.warren.state.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class Rpl353HandlerTests {

    lateinit var handler: Rpl353Handler
    lateinit var channelsState: ChannelsState
    lateinit var userPrefixesState: UserPrefixesState

    @Before fun setUp() {
        channelsState = ChannelsState(joined = mutableMapOf())
        userPrefixesState = UserPrefixesState(prefixesToModes = mapOf('@' to 'o', '+' to 'v'))
        val caseMappingState = CaseMappingState(mapping = CaseMapping.RFC1459)
        handler = Rpl353Handler(channelsState, userPrefixesState, caseMappingState)
    }

    @Test fun test_handle_WellFormed_AddsCorrectNicksToChannel() {
        channelsState.joined["#channel"] = ChannelState("#channel", users = generateUsers())

        handler.handle(Rpl353Message(source = "test.server", target = "test-nick", visibility = "=", channel = "#channel", names = listOf("@test-nick", "+another-person", "someone-else")))

        assertEquals(ChannelsState(joined = mutableMapOf("#channel" to ChannelState(name = "#channel", users = generateUsersWithModes(("test-nick" to setOf('o')), ("another-person" to setOf('v')), ("someone-else" to setOf()))))), channelsState)
    }

    @Test fun test_handle_MalformedUserNick_ProcessesTheRestAnyway() {
        channelsState.joined["#channel"] = ChannelState("#channel", users = generateUsers())

        handler.handle(Rpl353Message(source = "test.server", target = "test-nick", visibility = "=", channel = "#channel", names = listOf("@", "+another-person", "someone-else")))

        assertEquals(ChannelsState(joined = mutableMapOf("#channel" to ChannelState(name = "#channel", users = generateUsersWithModes(("another-person" to setOf('v')), ("someone-else" to setOf()))))), channelsState)
    }

    @Test fun test_handle_NotInChannel_DoesNothing() {
        handler.handle(Rpl353Message(source = "test.server", target = "test-nick", visibility = "=", channel = "#channel", names = listOf("@test-nick", "+another-person", "someone-else")))

        assertEquals(ChannelsState(joined = mutableMapOf()), channelsState)
    }
}