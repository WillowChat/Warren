package chat.willow.warren.handler.rpl

import chat.willow.kale.IMetadataStore
import chat.willow.kale.KaleHandler
import chat.willow.kale.irc.message.rfc1459.rpl.Rpl471Message
import chat.willow.kale.irc.message.rfc1459.rpl.Rpl471MessageType
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.CaseMappingState
import chat.willow.warren.state.JoiningChannelLifecycle
import chat.willow.warren.state.JoiningChannelsState

class Rpl471Handler(val channelsState: JoiningChannelsState, val caseMappingState: CaseMappingState) : KaleHandler<Rpl471MessageType>(Rpl471Message.Parser) {

    private val LOGGER = loggerFor<Rpl471Handler>()


    override fun handle(message: Rpl471MessageType, metadata: IMetadataStore) {
        val channel = channelsState[message.channel]

        if (channel == null) {
            LOGGER.warn("got a full channel reply for a channel we don't think we're joining: $message")
            LOGGER.trace("channels state: $channelsState")
            return
        }

        LOGGER.warn("channel is full, failed to join: $channel")
        channel.status = JoiningChannelLifecycle.FAILED

        LOGGER.trace("new channels state: $channelsState")
    }

}

