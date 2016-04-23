package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rfc1459.TopicMessage
import engineer.carrot.warren.warren.state.ChannelsState

class TopicHandler(val channelsState: ChannelsState) : IKaleHandler<TopicMessage> {
    override val messageType = TopicMessage::class.java

    override fun handle(message: TopicMessage) {
        val channel = channelsState.joined[message.channel]
        val topic = message.topic

        if (channel == null) {
            println("got a topic for a channel we don't think we're in, not doing anything: $message")
            return
        }

        println("channel topic for ${channel.name}: $topic")
        channel.topic = topic
    }
}

