package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.irc.message.rfc1459.TopicMessage
import engineer.carrot.warren.kale.irc.message.utility.CaseMapping
import engineer.carrot.warren.warren.state.CaseMappingState
import engineer.carrot.warren.warren.state.ChannelState
import engineer.carrot.warren.warren.state.ChannelsState
import engineer.carrot.warren.warren.state.generateUsers
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TopicHandlerTests {

    lateinit var handler: TopicHandler
    lateinit var channelsState: ChannelsState

    @Before fun setUp() {
        channelsState = ChannelsState(joined = mutableMapOf())
        val caseMappingState = CaseMappingState(mapping = CaseMapping.RFC1459)
        handler = TopicHandler(channelsState, caseMappingState)
    }

    @Test fun test_handle_NonexistentChannel_DoesNothing() {
        channelsState.joined["#channel"] = ChannelState(name = "#channel", users = generateUsers("test-nick"))

        handler.handle(TopicMessage(channel = "#somewhere", topic = "test topic"))

        val expectedChannelState = ChannelState(name = "#channel", users = generateUsers("test-nick"))

        assertEquals(ChannelsState(joined = mutableMapOf("#channel" to expectedChannelState)), channelsState)
    }

    @Test fun test_handle_ValidChannel_SetsTopic() {
        channelsState.joined["#channel"] = ChannelState(name = "#channel", users = generateUsers("test-nick"))

        handler.handle(TopicMessage(channel = "#channel", topic = "test topic"))

        val expectedChannelState = ChannelState(name = "#channel", users = generateUsers("test-nick"), topic = "test topic")

        assertEquals(ChannelsState(joined = mutableMapOf("#channel" to expectedChannelState)), channelsState)
    }

}