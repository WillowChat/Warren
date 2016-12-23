package engineer.carrot.warren.warren.handler.rpl

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rfc1459.rpl.Rpl475Message
import engineer.carrot.warren.warren.helper.loggerFor
import engineer.carrot.warren.warren.state.CaseMappingState
import engineer.carrot.warren.warren.state.JoiningChannelLifecycle
import engineer.carrot.warren.warren.state.JoiningChannelsState

class Rpl475Handler(val channelsState: JoiningChannelsState, val caseMappingState: CaseMappingState) : IKaleHandler<Rpl475Message> {

    private val LOGGER = loggerFor<Rpl475Handler>()

    override val messageType = Rpl475Message::class.java

    override fun handle(message: Rpl475Message, tags: Map<String, String?>) {
        val channel = channelsState[message.channel]

        if (channel == null) {
            LOGGER.warn("got a bad key channel reply for a channel we don't think we're joining: $message")
            LOGGER.trace("channels state: $channelsState")
            return
        }

        LOGGER.warn("channel key wrong, failed to join: $channel")
        channel.status = JoiningChannelLifecycle.FAILED

        LOGGER.trace("new channels state: $channelsState")
    }

}

