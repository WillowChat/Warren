package chat.willow.warren.handler

import chat.willow.kale.IMetadataStore
import chat.willow.kale.KaleHandler
import chat.willow.kale.helper.equalsIgnoreCase
import chat.willow.kale.irc.message.rfc1459.PartMessage
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.CaseMappingState
import chat.willow.warren.state.ConnectionState
import chat.willow.warren.state.JoinedChannelsState

class PartHandler(val connectionState: ConnectionState, val channelsState: JoinedChannelsState, val caseMappingState: CaseMappingState) : KaleHandler<PartMessage.Message>(PartMessage.Message.Parser) {

    private val LOGGER = loggerFor<PartHandler>()


    override fun handle(message: PartMessage.Message, metadata: IMetadataStore) {
        val channelNames = message.channels
        val source = message.source

        val nick = source.nick

        if (equalsIgnoreCase(caseMappingState.mapping, nick, connectionState.nickname)) {
            // Us parting a channel

            LOGGER.debug("we parted channels: ${message.channels}")

            for (channelName in channelNames) {
                if (channelsState.contains(channelName)) {
                    LOGGER.trace("removing $channelName from joined channels")

                    channelsState -= channelName
                } else {
                    LOGGER.trace("we already left $channelName - not leaving it again")
                }
            }
        } else {
            // Someone else parted a channel

            LOGGER.debug("$nick left $channelNames")

            for (channelName in channelNames) {
                val channelState = channelsState[channelName]
                if (channelState != null) {
                    channelState.users.remove(nick)

                    LOGGER.trace("new channel state: $channelState")
                } else {
                    LOGGER.trace("we were given a PART for a user who we don't think is in the channel - not doing anything with it: $channelName")
                }
            }
        }
    }

}