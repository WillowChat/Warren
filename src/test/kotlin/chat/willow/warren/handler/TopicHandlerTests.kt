package chat.willow.warren.handler

import chat.willow.kale.helper.CaseMapping
import chat.willow.kale.irc.message.rfc1459.TopicMessage
import chat.willow.kale.irc.prefix.prefix
import chat.willow.kale.irc.tag.TagStore
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
        channelsState.joined += ChannelState(name = "#channel", users = generateUsersFromNicks(listOf("test-nick"), mappingState = caseMappingState))

        handler.handle(TopicMessage.Message(channel = "#somewhere", topic = "test topic", source = prefix("")), TagStore())

        val expectedChannelState = ChannelState(name = "#channel", users = generateUsersFromNicks(listOf("test-nick"), mappingState = caseMappingState))

        assertEquals(channelsStateWith(listOf(expectedChannelState), caseMappingState), channelsState)
    }

    @Test fun test_handle_ValidChannel_SetsTopic() {
        channelsState.joined += ChannelState(name = "#channel", users = generateUsersFromNicks(listOf("test-nick"), mappingState = caseMappingState))

        handler.handle(TopicMessage.Message(channel = "#channel", topic = "test topic", source = prefix("")), TagStore())

        val expectedChannelState = ChannelState(name = "#channel", users = generateUsersFromNicks(listOf("test-nick"), mappingState = caseMappingState), topic = "test topic")

        assertEquals(channelsStateWith(listOf(expectedChannelState), caseMappingState), channelsState)
    }

}