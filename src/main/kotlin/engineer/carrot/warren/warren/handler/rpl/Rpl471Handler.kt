package engineer.carrot.warren.warren.handler.rpl

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rfc1459.rpl.Rpl471Message
import engineer.carrot.warren.warren.loggerFor
import engineer.carrot.warren.warren.state.CaseMappingState
import engineer.carrot.warren.warren.state.JoiningChannelLifecycle
import engineer.carrot.warren.warren.state.JoiningChannelsState

class Rpl471Handler(val channelsState: JoiningChannelsState, val caseMappingState: CaseMappingState) : IKaleHandler<Rpl471Message> {

    private val LOGGER = loggerFor<Rpl471Handler>()

    override val messageType = Rpl471Message::class.java

    override fun handle(message: Rpl471Message, tags: Map<String, String?>) {
        val channel = channelsState[message.channel]

        if (channel == null) {
            LOGGER.warn("got a full channel reply for a channel we don't think we're joining: $message")
            LOGGER.trace("channels state: $channelsState")
            return
        }

        LOGGER.warn("channel is full, failed to join: $channel")
        channel.status = JoiningChannelLifecycle.FAILED

        LOGGER.trace("new channels state: $channelsState")
    }

}

