package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rfc1459.NickMessage
import engineer.carrot.warren.kale.irc.message.rfc1459.PongMessage
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.loggerFor
import engineer.carrot.warren.warren.state.ChannelsState
import engineer.carrot.warren.warren.state.ConnectionState
import engineer.carrot.warren.warren.state.generateUser

class NickHandler(val connectionState: ConnectionState, val channelsState: ChannelsState) : IKaleHandler<NickMessage> {
    private val LOGGER = loggerFor<NickHandler>()

    override val messageType = NickMessage::class.java

    override fun handle(message: NickMessage) {
        val from = message.source?.nick
        val to = message.nickname

        if (from == null) {
            LOGGER.warn("from nick was missing, not doing anything: $message")
            return
        }

        if (from == connectionState.nickname) {
            // We were forcibly renamed by the server

            connectionState.nickname = from
        }

        for ((name, channel) in channelsState.joined) {
            if (channel.users.contains(from)) {
                channel.users.remove(from)
                channel.users += generateUser(to)
            }
        }

        LOGGER.trace("someone changed nick - new states: $connectionState, $channelsState")
    }
}