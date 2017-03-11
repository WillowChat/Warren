package chat.willow.warren.handler

import chat.willow.kale.IKaleHandler
import chat.willow.kale.irc.message.rfc1459.JoinMessage
import chat.willow.kale.irc.message.utility.equalsIgnoreCase
import chat.willow.kale.irc.tag.ITagStore
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.*

class JoinHandler(val connectionState: ConnectionState, val joiningChannelsState: JoiningChannelsState, val joinedChannelsState: JoinedChannelsState, val caseMappingState: CaseMappingState) : IKaleHandler<JoinMessage> {

    private val LOGGER = loggerFor<JoinHandler>()

    override val messageType = JoinMessage::class.java

    override fun handle(message: JoinMessage, tags: ITagStore) {
        val channelNames = message.channels
        val source = message.source

        if (source == null) {
            LOGGER.trace("got a JOIN but the source was null - not doing anything with it")
            return
        }

        val nick = source.nick

        if (equalsIgnoreCase(caseMappingState.mapping, nick, connectionState.nickname)) {
            // Us joining a channel

            LOGGER.debug("we joined channels: ${message.channels}")

            for (channelName in channelNames) {
                if (!joinedChannelsState.contains(channelName)) {
                    LOGGER.trace("adding $channelName to joined channels with 0 users")

                    joinedChannelsState += emptyChannel(name = channelName, mappingState = caseMappingState)
                } else {
                    LOGGER.trace("we're already in $channelName - not adding it again")
                }

                LOGGER.trace("removing channel from joining state: $channelName")
                joiningChannelsState -= channelName
            }
        } else {
            // Someone else joined a channel

            for (channelName in channelNames) {
                val channelState = joinedChannelsState[channelName]
                if (channelState != null) {
                    channelState.users += generateUser(nick)

                    LOGGER.trace("new channel state: $channelState")
                } else {
                    LOGGER.warn("we were given a JOIN for a channel we aren't in - not doing anything with it: $channelName")
                }
            }
        }
    }

}