package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.irc.message.rfc1459.NickMessage
import engineer.carrot.warren.kale.irc.prefix.Prefix
import engineer.carrot.warren.warren.state.ChannelState
import engineer.carrot.warren.warren.state.ChannelsState
import engineer.carrot.warren.warren.state.ConnectionState
import engineer.carrot.warren.warren.state.LifecycleState
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class NickHandlerTests {

    lateinit var handler: NickHandler
    lateinit var connectionState: ConnectionState
    lateinit var channelsState: ChannelsState

    @Before fun setUp() {
        val lifecycleState = LifecycleState.DISCONNECTED
        connectionState = ConnectionState(server = "test.server", port = 6697, nickname = "test-nick", username = "test-user", lifecycle = lifecycleState)
        channelsState = ChannelsState(joined = mutableMapOf())
        handler = NickHandler(connectionState, channelsState)
    }

    @Test fun test_handle_FromIsNull_DoesNothing() {
        channelsState.joined["#channel"] = ChannelState(name = "#channel", users = mutableSetOf("someone", "someone-else"))
        channelsState.joined["#channel2"] = ChannelState(name = "#channel2", users = mutableSetOf("another-person", "someone-else"))

        handler.handle(NickMessage(nickname = "someone-else-2"))

        val expectedChannelOneState = ChannelState(name = "#channel", users = mutableSetOf("someone", "someone-else"))
        val expectedChannelTwoState = ChannelState(name = "#channel2", users = mutableSetOf("another-person", "someone-else"))
        val expectedChannelsState = ChannelsState(joined = mutableMapOf("#channel" to expectedChannelOneState, "#channel2" to expectedChannelTwoState))

        assertEquals(channelsState, expectedChannelsState)
    }

    @Test fun test_handle_FromIsSelf_RenamesSelf() {
        channelsState.joined["#channel"] = ChannelState(name = "#channel", users = mutableSetOf("test-nick", "someone-else"))
        channelsState.joined["#channel2"] = ChannelState(name = "#channel2", users = mutableSetOf("another-person", "someone-else"))

        handler.handle(NickMessage(source = Prefix(nick = "test-nick", host = "somewhere"), nickname = "test-new-nick"))

        val expectedChannelOneState = ChannelState(name = "#channel", users = mutableSetOf("test-new-nick", "someone-else"))
        val expectedChannelTwoState = ChannelState(name = "#channel2", users = mutableSetOf("another-person", "someone-else"))
        val expectedChannelsState = ChannelsState(joined = mutableMapOf("#channel" to expectedChannelOneState, "#channel2" to expectedChannelTwoState))

        assertEquals(channelsState, expectedChannelsState)
    }

    @Test fun test_handle_FromIsSomeoneElse_UserIsRenamedInAllChannels() {
        channelsState.joined["#channel"] = ChannelState(name = "#channel", users = mutableSetOf("someone", "someone-else"))
        channelsState.joined["#channel2"] = ChannelState(name = "#channel2", users = mutableSetOf("another-person", "someone-else"))

        handler.handle(NickMessage(source = Prefix(nick = "someone-else", host = "somewhere"), nickname = "someone-else-2"))

        val expectedChannelOneState = ChannelState(name = "#channel", users = mutableSetOf("someone", "someone-else-2"))
        val expectedChannelTwoState = ChannelState(name = "#channel2", users = mutableSetOf("another-person", "someone-else-2"))
        val expectedChannelsState = ChannelsState(joined = mutableMapOf("#channel" to expectedChannelOneState, "#channel2" to expectedChannelTwoState))

        assertEquals(channelsState, expectedChannelsState)
    }

}