package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rfc1459.KickMessage
import engineer.carrot.warren.warren.loggerFor
import engineer.carrot.warren.warren.state.CaseMappingState
import engineer.carrot.warren.warren.state.ChannelsState
import engineer.carrot.warren.warren.state.ConnectionState

class KickHandler(val connectionState: ConnectionState, val channelsState: ChannelsState, val caseMappingState: CaseMappingState) : IKaleHandler<KickMessage> {
    private val LOGGER = loggerFor<KickHandler>()

    override val messageType = KickMessage::class.java

    override fun handle(message: KickMessage) {
        val kickedNicks = message.users
        val channels = message.channels

        for (kickedNick in kickedNicks) {
            if (kickedNick == connectionState.nickname) {
                // We were forcibly kicked

                val removedChannels = channels.map { channel -> channelsState.removeJoined(channel, caseMappingState.mapping) }
                LOGGER.debug("we were kicked from channels: $removedChannels")
            } else {
                // Someone else was kicked

                for ((name, channel) in channelsState.joined) {
                    if (channel.users.contains(kickedNick)) {
                        channel.users.remove(kickedNick)
                    }
                }
            }
        }

        LOGGER.trace("kicks happened - new channels state: $channelsState")
    }
}