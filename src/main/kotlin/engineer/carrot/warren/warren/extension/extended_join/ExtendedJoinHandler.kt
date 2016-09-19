package engineer.carrot.warren.warren.extension.extended_join

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.ircv3.ExtendedJoinMessage
import engineer.carrot.warren.kale.irc.message.rfc1459.JoinMessage
import engineer.carrot.warren.warren.loggerFor
import engineer.carrot.warren.warren.state.CaseMappingState
import engineer.carrot.warren.warren.state.ChannelsState

class ExtendedJoinHandler(val joinHandler: IKaleHandler<JoinMessage>, val channelsState: ChannelsState, val caseMappingState: CaseMappingState) : IKaleHandler<ExtendedJoinMessage> {

    private val LOGGER = loggerFor<ExtendedJoinHandler>()
    override val messageType = ExtendedJoinMessage::class.java

    override fun handle(message: ExtendedJoinMessage, tags: Map<String, String?>) {
        joinHandler.handle(JoinMessage(source = message.source, channels = listOf(message.channel)), tags = tags)

        val nick = message.source.nick
        val channelName = message.channel
        val account = message.account

        val channelState = channelsState.joined[channelName]
        if (channelState != null) {
            for ((name, channel) in channelsState.joined.all) {
                val user = channel.users[nick]
                if (user != null) {
                    channel.users -= nick
                    channel.users += user.copy(account = account)
                }
            }

            LOGGER.trace("extended JOIN, new channel state: ${channelState}")
        } else {
            LOGGER.warn("we were given an extended JOIN for a channel we aren't in - not doing anything with it: $channelName")
        }
    }

}