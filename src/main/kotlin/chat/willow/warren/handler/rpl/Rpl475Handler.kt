package chat.willow.warren.handler.rpl

import chat.willow.kale.IMetadataStore
import chat.willow.kale.KaleHandler
import chat.willow.kale.irc.message.rfc1459.rpl.Rpl475Message
import chat.willow.kale.irc.message.rfc1459.rpl.Rpl475MessageType
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.CaseMappingState
import chat.willow.warren.state.JoiningChannelLifecycle
import chat.willow.warren.state.JoiningChannelsState

class Rpl475Handler(val channelsState: JoiningChannelsState, val caseMappingState: CaseMappingState) : KaleHandler<Rpl475MessageType>(Rpl475Message.Parser) {

    private val LOGGER = loggerFor<Rpl475Handler>()


    override fun handle(message: Rpl475MessageType, metadata: IMetadataStore) {
        val channel = channelsState[message.channel]

        if (channel == null) {
            LOGGER.warn("got a bad key channel reply for a channel we don't think we're joining: $message")
            LOGGER.trace("channels state: $channelsState")
            return
        }

        LOGGER.warn("channel key wrong, failed to join: $channel")
        channel.status = JoiningChannelLifecycle.FAILED

        LOGGER.trace("new channels state: $channelsState")
    }

}

