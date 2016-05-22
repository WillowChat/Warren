package engineer.carrot.warren.warren.handler.rpl

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rpl.Rpl473Message
import engineer.carrot.warren.warren.loggerFor
import engineer.carrot.warren.warren.state.CaseMappingState
import engineer.carrot.warren.warren.state.JoiningChannelLifecycle
import engineer.carrot.warren.warren.state.JoiningChannelsState

class Rpl473Handler(val channelsState: JoiningChannelsState, val caseMappingState: CaseMappingState) : IKaleHandler<Rpl473Message> {
    private val LOGGER = loggerFor<Rpl473Handler>()

    override val messageType = Rpl473Message::class.java

    override fun handle(message: Rpl473Message) {
        val channel = channelsState[message.channel]

        if (channel == null) {
            LOGGER.warn("got an invite only channel reply for a channel we don't think we're joining: $message")
            LOGGER.trace("channels state: $channelsState")
            return
        }

        LOGGER.warn("channel is invite only, failed to join: $channel")
        channel.status = JoiningChannelLifecycle.FAILED

        LOGGER.trace("new channels state: $channelsState")
    }
}

