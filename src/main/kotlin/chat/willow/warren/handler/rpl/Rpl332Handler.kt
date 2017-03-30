package chat.willow.warren.handler.rpl

import chat.willow.kale.IMetadataStore
import chat.willow.kale.KaleHandler
import chat.willow.kale.irc.message.rfc1459.rpl.Rpl332Message
import chat.willow.kale.irc.message.rfc1459.rpl.Rpl332MessageType
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.CaseMappingState
import chat.willow.warren.state.JoinedChannelsState

class Rpl332Handler(val channelsState: JoinedChannelsState, val caseMappingState: CaseMappingState) : KaleHandler<Rpl332MessageType>(Rpl332Message.Parser) {

    private val LOGGER = loggerFor<Rpl332Handler>()


    override fun handle(message: Rpl332MessageType, metadata: IMetadataStore) {
        val channel = channelsState[message.channel]
        val topic = message.content

        if (channel == null) {
            LOGGER.warn("got a topic for a channel we don't think we're in, not doing anything: $message")
            return
        }

        LOGGER.debug("channel topic for ${channel.name}: $topic")
        channel.topic = topic
    }

}

