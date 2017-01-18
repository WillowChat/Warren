package chat.willow.warren.handler

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rfc1459.PartMessage
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.CaseMappingState
import chat.willow.warren.state.ConnectionState
import chat.willow.warren.state.JoinedChannelsState

class PartHandler(val connectionState: ConnectionState, val channelsState: JoinedChannelsState, val caseMappingState: CaseMappingState) : IKaleHandler<PartMessage> {

    private val LOGGER = loggerFor<PartHandler>()

    override val messageType = PartMessage::class.java

    override fun handle(message: PartMessage, tags: Map<String, String?>) {
        val channelNames = message.channels
        val source = message.source

        if (source == null) {
            LOGGER.warn("got a PART but the source was null - not doing anything with it")
            return
        }

        val nick = source.nick

        if (nick == connectionState.nickname) {
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