package engineer.carrot.warren.warren.handler.rpl

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rfc1459.JoinMessage
import engineer.carrot.warren.kale.irc.message.rpl.Rpl475Message
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.loggerFor
import engineer.carrot.warren.warren.state.ChannelsState
import engineer.carrot.warren.warren.state.ConnectionState
import engineer.carrot.warren.warren.state.JoiningChannelLifecycle
import engineer.carrot.warren.warren.state.LifecycleState

class Rpl475Handler(val channelsState: ChannelsState) : IKaleHandler<Rpl475Message> {
    private val LOGGER = loggerFor<Rpl475Handler>()

    override val messageType = Rpl475Message::class.java

    override fun handle(message: Rpl475Message) {
        val channel = channelsState.joining[message.channel]

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

