package engineer.carrot.warren.warren.handler

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import engineer.carrot.warren.kale.irc.message.rfc1459.PartMessage
import engineer.carrot.warren.kale.irc.prefix.Prefix
import engineer.carrot.warren.warren.state.ChannelState
import engineer.carrot.warren.warren.state.ChannelsState
import engineer.carrot.warren.warren.state.ConnectionState
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class PartHandlerTests {

    lateinit var handler: PartHandler
    lateinit var connectionState: ConnectionState
    lateinit var channelsState: ChannelsState

    @Before fun setUp() {
        connectionState = ConnectionState(server = "test.server", port = 6697, nickname = "test-nick", username = "test-user")
        channelsState = ChannelsState(joined = mutableMapOf())
        handler = PartHandler(connectionState, channelsState)
    }

    @Test fun test_handle_SourceIsSelf_WellFormed_PartsCorrectChannel() {
        channelsState.joined["#channel"] = ChannelState("#channel", users = mutableSetOf("test-nick"))

        handler.handle(PartMessage(source = Prefix(nick = "test-nick"), channels = listOf("#channel")))

        assertEquals(ChannelsState(joined = mutableMapOf()), channelsState)
    }

    @Test fun test_handle_SourceIsSelf_NotInChannel() {
        handler.handle(PartMessage(source = Prefix(nick = "test-nick"), channels = listOf("#channel")))

        assertEquals(ChannelsState(joined = mutableMapOf()), channelsState)
    }

    @Test fun test_handle_SourceIsSelf_MissingSource_DoesNothing() {
        handler.handle(PartMessage(channels = listOf("#channel")))

        assertEquals(ChannelsState(joined = mutableMapOf()), channelsState)
    }

    @Test fun test_handle_SourceIsOther_WellFormed() {
        channelsState.joined["#channel"] = ChannelState("#channel", users = mutableSetOf("test-nick", "someone-else"))

        handler.handle(PartMessage(source = Prefix(nick = "someone-else"), channels = listOf("#channel")))

        assertEquals(ChannelsState(joined = mutableMapOf("#channel" to ChannelState(name = "#channel", users = mutableSetOf("test-nick")))), channelsState)
    }

    @Test fun test_handle_SourceIsOther_NotInChannel() {
        handler.handle(PartMessage(source = Prefix(nick = "someone-else"), channels = listOf("#channel")))

        assertEquals(ChannelsState(joined = mutableMapOf()), channelsState)
    }

}