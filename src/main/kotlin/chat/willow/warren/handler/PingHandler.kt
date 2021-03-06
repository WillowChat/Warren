package chat.willow.warren.handler

import chat.willow.kale.IMetadataStore
import chat.willow.kale.KaleHandler
import chat.willow.kale.irc.message.rfc1459.PingMessage
import chat.willow.kale.irc.message.rfc1459.PongMessage
import chat.willow.warren.IMessageSink
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.ConnectionState

class PingHandler(val sink: IMessageSink, val connectionState: ConnectionState) : KaleHandler<PingMessage.Command>(PingMessage.Command.Parser) {

    private val LOGGER = loggerFor<PingHandler>()


    override fun handle(message: PingMessage.Command, metadata: IMetadataStore) {
        LOGGER.debug("handling ping with token ${message.token}")

        sink.write(PongMessage.Message(token = message.token))

        connectionState.lastPingOrPong = System.currentTimeMillis()
    }

}