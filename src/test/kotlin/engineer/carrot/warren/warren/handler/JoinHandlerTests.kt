package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.irc.message.rfc1459.JoinMessage
import engineer.carrot.warren.kale.irc.prefix.Prefix
import engineer.carrot.warren.warren.state.ChannelState
import engineer.carrot.warren.warren.state.ChannelsState
import engineer.carrot.warren.warren.state.ConnectionState
import engineer.carrot.warren.warren.state.LifecycleState
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class JoinHandlerTests {

    lateinit var handler: JoinHandler
    lateinit var connectionState: ConnectionState
    lateinit var channelsState: ChannelsState

    @Before fun setUp() {
        val lifecycleState = LifecycleState.DISCONNECTED
        connectionState = ConnectionState(server = "test.server", port = 6697, nickname = "test-nick", username = "test-nick", lifecycle = lifecycleState)
        channelsState = ChannelsState(joined = mutableMapOf())
        handler = JoinHandler(connectionState, channelsState)
    }

    @Test fun test_handle_SourceIsSelf_WellFormed_JoinsCorrectChannel() {
        handler.handle(JoinMessage(source = Prefix(nick = "test-nick"), channels = listOf("#channel")))

        assertEquals(ChannelsState(joined = mutableMapOf("#channel" to ChannelState(name = "#channel", users = mutableSetOf()))), channelsState)
    }

    @Test fun test_handle_SourceIsSelf_AlreadyInChannel() {
        channelsState.joined["#channel"] = ChannelState("#channel", users = mutableSetOf("test-nick"))

        handler.handle(JoinMessage(source = Prefix(nick = "test-nick"), channels = listOf("#channel")))

        assertEquals(ChannelsState(joined = mutableMapOf("#channel" to ChannelState(name = "#channel", users = mutableSetOf("test-nick")))), channelsState)
    }

    @Test fun test_handle_SourceIsSelf_MissingSource_DoesNothing() {
        handler.handle(JoinMessage(channels = listOf("#channel")))

        assertEquals(ChannelsState(joined = mutableMapOf()), channelsState)
    }

    @Test fun test_handle_SourceIsOther_WellFormed() {
        channelsState.joined["#channel"] = ChannelState("#channel", users = mutableSetOf("test-nick"))

        handler.handle(JoinMessage(source = Prefix(nick = "someone-else"), channels = listOf("#channel")))

        assertEquals(ChannelsState(joined = mutableMapOf("#channel" to ChannelState(name = "#channel", users = mutableSetOf("test-nick", "someone-else")))), channelsState)
    }

    @Test fun test_handle_SourceIsOther_AlreadyInChannel() {
        channelsState.joined["#channel"] = ChannelState("#channel", users = mutableSetOf("someone-else"))

        handler.handle(JoinMessage(source = Prefix(nick = "someone-else"), channels = listOf("#channel")))

        assertEquals(ChannelsState(joined = mutableMapOf("#channel" to ChannelState(name = "#channel", users = mutableSetOf("someone-else")))), channelsState)
    }

    @Test fun test_handle_SourceIsOther_NotInChannel() {
        handler.handle(JoinMessage(source = Prefix(nick = "someone-else"), channels = listOf("#channel")))

        assertEquals(ChannelsState(joined = mutableMapOf()), channelsState)
    }

}