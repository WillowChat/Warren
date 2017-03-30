package chat.willow.warren.handler

import chat.willow.kale.IMetadataStore
import chat.willow.kale.KaleHandler
import chat.willow.kale.irc.message.rfc1459.PongMessage
import chat.willow.warren.IMessageSink
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.ConnectionState

class PongHandler(val sink: IMessageSink, val connectionState: ConnectionState) : KaleHandler<PongMessage.Message>(PongMessage.Message.Parser) {

    private val LOGGER = loggerFor<PongHandler>()


    override fun handle(message: PongMessage.Message, metadata: IMetadataStore) {
        LOGGER.debug("got pong with token ${message.token}")

        connectionState.lastPingOrPong = System.currentTimeMillis()
    }

}