package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.irc.message.rpl.Rpl353Message
import engineer.carrot.warren.warren.state.ChannelState
import engineer.carrot.warren.warren.state.ChannelsState
import engineer.carrot.warren.warren.state.UserPrefixesState
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
        handler = Rpl353Handler(channelsState, userPrefixesState)
    }

    @Test fun test_handle_WellFormed_AddsCorrectNicksToChannel() {
        channelsState.joined["#channel"] = ChannelState("#channel", users = mutableSetOf())

        handler.handle(Rpl353Message(source = "test.server", target = "test-nick", visibility = "=", channel = "#channel", names = listOf("@test-nick", "+another-person", "someone-else")))

        assertEquals(ChannelsState(joined = mutableMapOf("#channel" to ChannelState(name = "#channel", users = mutableSetOf("test-nick", "another-person", "someone-else")))), channelsState)
    }

    @Test fun test_handle_MalformedUserNick_ProcessesTheRestAnyway() {
        channelsState.joined["#channel"] = ChannelState("#channel", users = mutableSetOf())

        handler.handle(Rpl353Message(source = "test.server", target = "test-nick", visibility = "=", channel = "#channel", names = listOf("@", "+another-person", "someone-else")))

        assertEquals(ChannelsState(joined = mutableMapOf("#channel" to ChannelState(name = "#channel", users = mutableSetOf("another-person", "someone-else")))), channelsState)
    }

    @Test fun test_handle_NotInChannel_DoesNothing() {
        handler.handle(Rpl353Message(source = "test.server", target = "test-nick", visibility = "=", channel = "#channel", names = listOf("@test-nick", "+another-person", "someone-else")))

        assertEquals(ChannelsState(joined = mutableMapOf()), channelsState)
    }
}