package engineer.carrot.warren.warren.extension.away_notify

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.ircv3.AwayMessage
import engineer.carrot.warren.warren.loggerFor
import engineer.carrot.warren.warren.state.JoinedChannelsState

class AwayHandler(val channelsState: JoinedChannelsState) : IKaleHandler<AwayMessage> {

    private val LOGGER = loggerFor<AwayHandler>()
    override val messageType = AwayMessage::class.java

    override fun handle(message: AwayMessage, tags: Map<String, String?>) {
        val nick = message.source.nick
        val awayMessage = message.message

        for ((name, channel) in channelsState.all) {
            val user = channel.users[nick]
            if (user != null) {
                channel.users -= nick
                channel.users += user.copy(awayMessage = awayMessage)
            }
        }

        if (awayMessage == null) {
            LOGGER.trace("user is no longer away: ${message.source}")
        } else {
            LOGGER.trace("user is away: ${message.source} '$awayMessage'")
        }
    }

}