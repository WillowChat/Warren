package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.irc.message.rfc1459.QuitMessage
import engineer.carrot.warren.kale.irc.prefix.Prefix
import engineer.carrot.warren.warren.state.ChannelState
import engineer.carrot.warren.warren.state.ChannelsState
import engineer.carrot.warren.warren.state.ConnectionState
import engineer.carrot.warren.warren.state.LifecycleState
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class QuitHandlerTests {

    lateinit var handler: QuitHandler
    lateinit var connectionState: ConnectionState
    lateinit var channelsState: ChannelsState

    @Before fun setUp() {
        val lifecycleState = LifecycleState.CONNECTED
        connectionState = ConnectionState(server = "test.server", port = 6697, nickname = "test-nick", username = "test-user", lifecycle = lifecycleState)
        channelsState = ChannelsState(joined = mutableMapOf())
        handler = QuitHandler(connectionState, channelsState)
    }

    @Test fun test_handle_SourceIsNull_DoesNothing() {
        channelsState.joined["#channel"] = ChannelState(name = "#channel", users = mutableSetOf("someone", "someone-else"))

        handler.handle(QuitMessage())

        val expectedChannelOneState = ChannelState(name = "#channel", users = mutableSetOf("someone", "someone-else"))
        val expectedChannelsState = ChannelsState(joined = mutableMapOf("#channel" to expectedChannelOneState))

        assertEquals(channelsState, expectedChannelsState)
    }

    @Test fun test_handle_SourceIsSelf_ChangesConnectionStateToDisconnected() {
        channelsState.joined["#channel"] = ChannelState(name = "#channel", users = mutableSetOf("test-nick", "someone-else"))
        channelsState.joined["#channel2"] = ChannelState(name = "#channel2", users = mutableSetOf("another-person", "someone-else"))

        handler.handle(QuitMessage(source = Prefix(nick = "test-nick")))

        val expectedChannelOneState = ChannelState(name = "#channel", users = mutableSetOf("test-nick", "someone-else"))
        val expectedChannelTwoState = ChannelState(name = "#channel2", users = mutableSetOf("another-person", "someone-else"))
        val expectedChannelsState = ChannelsState(joined = mutableMapOf("#channel" to expectedChannelOneState, "#channel2" to expectedChannelTwoState))

        assertEquals(channelsState, expectedChannelsState)
        assertEquals(connectionState.lifecycle, LifecycleState.DISCONNECTED)
    }

    @Test fun test_handle_SourceIsOther_RemovesUserFromChannels() {
        channelsState.joined["#channel"] = ChannelState(name = "#channel", users = mutableSetOf("test-nick", "someone-else"))
        channelsState.joined["#channel2"] = ChannelState(name = "#channel2", users = mutableSetOf("another-person", "someone-else"))

        handler.handle(QuitMessage(source = Prefix(nick = "someone-else")))

        val expectedChannelOneState = ChannelState(name = "#channel", users = mutableSetOf("test-nick"))
        val expectedChannelTwoState = ChannelState(name = "#channel2", users = mutableSetOf("another-person"))
        val expectedChannelsState = ChannelsState(joined = mutableMapOf("#channel" to expectedChannelOneState, "#channel2" to expectedChannelTwoState))

        assertEquals(channelsState, expectedChannelsState)
    }

}