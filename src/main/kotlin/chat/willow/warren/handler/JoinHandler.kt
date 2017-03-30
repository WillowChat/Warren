package chat.willow.warren.handler

import chat.willow.kale.IMetadataStore
import chat.willow.kale.KaleHandler
import chat.willow.kale.helper.equalsIgnoreCase
import chat.willow.kale.irc.message.rfc1459.JoinMessage
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.*

class JoinHandler(val connectionState: ConnectionState, val joiningChannelsState: JoiningChannelsState, val joinedChannelsState: JoinedChannelsState, val caseMappingState: CaseMappingState) : KaleHandler<JoinMessage.Message>(JoinMessage.Message.Parser) {

    private val LOGGER = loggerFor<JoinHandler>()


    override fun handle(message: JoinMessage.Message, metadata: IMetadataStore) {
        val channelNames = message.channels
        val source = message.source

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