package chat.willow.warren.handler

import chat.willow.kale.IKaleHandler
import chat.willow.kale.irc.message.rfc1459.NickMessage
import chat.willow.kale.irc.tag.ITagStore
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.ConnectionState
import chat.willow.warren.state.JoinedChannelsState

class NickHandler(val connectionState: ConnectionState, val channelsState: JoinedChannelsState) : IKaleHandler<NickMessage> {

    private val LOGGER = loggerFor<NickHandler>()

    override val messageType = NickMessage::class.java

    override fun handle(message: NickMessage, tags: ITagStore) {
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