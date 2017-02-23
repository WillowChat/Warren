package chat.willow.warren.extension.chghost

import chat.willow.kale.IKaleHandler
import chat.willow.kale.irc.message.extension.chghost.ChgHostMessage
import chat.willow.kale.irc.prefix.Prefix
import chat.willow.kale.irc.tag.ITagStore
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.JoinedChannelsState

class ChgHostHandler(val channelsState: JoinedChannelsState) : IKaleHandler<ChgHostMessage> {

    private val LOGGER = loggerFor<ChgHostHandler>()
    override val messageType = ChgHostMessage::class.java

    override fun handle(message: ChgHostMessage, tags: ITagStore) {
        val newUser = message.newUser
        val newHost = message.newHost

        val source = message.source
        val nick = source.nick

        val newPrefix = Prefix(nick = nick, user = newUser, host = newHost)

        for ((_, channel) in channelsState.all) {
            val user = channel.users[nick]
            if (user != null) {
                channel.users -= nick
                channel.users += user.copy(prefix = newPrefix)
            }
        }

        LOGGER.trace("user changed prefix from ${message.source} to $newPrefix")
    }

}