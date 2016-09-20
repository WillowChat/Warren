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
        val from = message.source
        val to = message.nickname

        if (from == null) {
            LOGGER.warn("from nick was missing, not doing anything: $message")
            return
        }

        if (from.nick == connectionState.nickname) {
            // We were forcibly renamed by the server

            connectionState.nickname = from.nick
        }

        for ((name, channel) in channelsState.all) {
            val user = channel.users[from.nick]
            if (user != null) {
                channel.users -= from.nick
                channel.users += user.copy(prefix = from.copy(nick = to))
            }
        }

        LOGGER.trace("someone changed nick - new states: $connectionState, $channelsState")
    }

}