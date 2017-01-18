package chat.willow.warren.handler

import chat.willow.kale.irc.message.rfc1459.TopicMessage
import chat.willow.kale.irc.message.utility.CaseMapping
import chat.willow.warren.state.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TopicHandlerTests {

    lateinit var handler: TopicHandler
    lateinit var channelsState: ChannelsState
    val caseMappingState = CaseMappingState(mapping = CaseMapping.RFC1459)

    @Before fun setUp() {
        channelsState = emptyChannelsState(caseMappingState)
        handler = TopicHandler(channelsState.joined, caseMappingState)
    }

    @Test fun test_handle_NonexistentChannel_DoesNothing() {
        channelsState.joined += ChannelState(name = "#channel", users = generateUsers("test-nick", mappingState = caseMappingState))

        handler.handle(TopicMessage(channel = "#somewhere", topic = "test topic"), mapOf())

        val expectedChannelState = ChannelState(name = "#channel", users = generateUsers("test-nick", mappingState = caseMappingState))

        assertEquals(channelsStateWith(listOf(expectedChannelState), caseMappingState), channelsState)
    }

    @Test fun test_handle_ValidChannel_SetsTopic() {
        channelsState.joined += ChannelState(name = "#channel", users = generateUsers("test-nick", mappingState = caseMappingState))

        handler.handle(TopicMessage(channel = "#channel", topic = "test topic"), mapOf())

        val expectedChannelState = ChannelState(name = "#channel", users = generateUsers("test-nick", mappingState = caseMappingState), topic = "test topic")

        assertEquals(channelsStateWith(listOf(expectedChannelState), caseMappingState), channelsState)
    }

}