package engineer.carrot.warren.warren.handler.rpl

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rpl.Rpl332Message
import engineer.carrot.warren.warren.loggerFor
import engineer.carrot.warren.warren.state.CaseMappingState
import engineer.carrot.warren.warren.state.JoinedChannelsState

class Rpl332Handler(val channelsState: JoinedChannelsState, val caseMappingState: CaseMappingState) : IKaleHandler<Rpl332Message> {
    private val LOGGER = loggerFor<Rpl332Handler>()

    override val messageType = Rpl332Message::class.java

    override fun handle(message: Rpl332Message) {
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

