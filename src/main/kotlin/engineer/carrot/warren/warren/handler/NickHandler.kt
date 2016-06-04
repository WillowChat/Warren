package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rfc1459.NickMessage
import engineer.carrot.warren.warren.loggerFor
import engineer.carrot.warren.warren.state.ConnectionState
import engineer.carrot.warren.warren.state.JoinedChannelsState

class NickHandler(val connectionState: ConnectionState, val channelsState: JoinedChannelsState) : IKaleHandler<NickMessage> {
    private val LOGGER = loggerFor<NickHandler>()

    override val messageType = NickMessage::class.java

    override fun handle(message: NickMessage, tags: Map<String, String?>) {
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

        for ((name, channel) in channelsState.all) {
            val user = channel.users[from]
            if (user != null) {
                channel.users -= from
                channel.users += user.copy(nick = to)
            }
        }

        LOGGER.trace("someone changed nick - new states: $connectionState, $channelsState")
    }
}