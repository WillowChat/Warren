package chat.willow.warren.extension.extended_join

import chat.willow.kale.IKaleMessageHandler
import chat.willow.kale.IMetadataStore
import chat.willow.kale.KaleHandler
import chat.willow.kale.irc.message.extension.extended_join.ExtendedJoinMessage
import chat.willow.kale.irc.message.rfc1459.JoinMessage
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.CaseMappingState
import chat.willow.warren.state.ChannelsState

class ExtendedJoinHandler(val joinHandler: IKaleMessageHandler<JoinMessage.Message>?, val channelsState: ChannelsState, val caseMappingState: CaseMappingState) : KaleHandler<ExtendedJoinMessage.Message>(ExtendedJoinMessage.Message.Parser) {

    private val LOGGER = loggerFor<ExtendedJoinHandler>()

    override fun handle(message: ExtendedJoinMessage.Message, metadata: IMetadataStore) {
        joinHandler?.handle(JoinMessage.Message(source = message.source, channels = listOf(message.channel)), metadata = metadata)

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

            LOGGER.trace("extended JOIN, new channel state: $channelState")
        } else {
            LOGGER.warn("we were given an extended JOIN for a channel we aren't in - not doing anything with it: $channelName")
        }
    }

}