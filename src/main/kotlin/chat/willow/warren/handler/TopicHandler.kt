package chat.willow.warren.handler

import chat.willow.kale.IKaleHandler
import chat.willow.kale.irc.message.rfc1459.TopicMessage
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.CaseMappingState
import chat.willow.warren.state.JoinedChannelsState

class TopicHandler(val channelsState: JoinedChannelsState, val caseMappingState: CaseMappingState) : IKaleHandler<TopicMessage> {

    private val LOGGER = loggerFor<TopicHandler>()

    override val messageType = TopicMessage::class.java

    override fun handle(message: TopicMessage, tags: Map<String, String?>) {
        val channel = channelsState[message.channel]
        val topic = message.topic

        if (channel == null) {
            LOGGER.warn("got a topic for a channel we don't think we're in, not doing anything: $message")
            return
        }

        LOGGER.debug("channel topic for ${channel.name}: $topic")
        channel.topic = topic
    }

}

