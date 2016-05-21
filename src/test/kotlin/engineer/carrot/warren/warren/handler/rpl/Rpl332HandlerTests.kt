package engineer.carrot.warren.warren.handler.rpl

import engineer.carrot.warren.kale.irc.message.rpl.Rpl332Message
import engineer.carrot.warren.warren.state.ChannelState
import engineer.carrot.warren.warren.state.ChannelsState
import engineer.carrot.warren.warren.state.generateUsers
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class Rpl332HandlerTests {

    lateinit var handler: Rpl332Handler
    lateinit var channelsState: ChannelsState

    @Before fun setUp() {
        channelsState = ChannelsState(joined = mutableMapOf())
        handler = Rpl332Handler(channelsState)
    }

    @Test fun test_handle_NonexistentChannel_DoesNothing() {
        channelsState.joined["#channel"] = ChannelState(name = "#channel", users = generateUsers("test-nick"))

        handler.handle(Rpl332Message(source = "", target = "", channel = "#somewhere", topic = "test topic"))

        val expectedChannelState = ChannelState(name = "#channel", users = generateUsers("test-nick"))

        assertEquals(ChannelsState(joined = mutableMapOf("#channel" to expectedChannelState)), channelsState)
    }

    @Test fun test_handle_ValidChannel_SetsTopic() {
        channelsState.joined["#channel"] = ChannelState(name = "#channel", users = generateUsers("test-nick"))

        handler.handle(Rpl332Message(source = "", target = "", channel = "#channel", topic = "test topic"))

        val expectedChannelState = ChannelState(name = "#channel", users = generateUsers("test-nick"), topic = "test topic")

        assertEquals(ChannelsState(joined = mutableMapOf("#channel" to expectedChannelState)), channelsState)
    }

}