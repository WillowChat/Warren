package chat.willow.warren.handler

import chat.willow.kale.IKaleHandler
import chat.willow.kale.irc.message.rfc1459.PongMessage
import chat.willow.kale.irc.tag.ITagStore
import chat.willow.warren.IMessageSink
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.ConnectionState

class PongHandler(val sink: IMessageSink, val connectionState: ConnectionState) : IKaleHandler<PongMessage> {

    private val LOGGER = loggerFor<PongHandler>()

    override val messageType = PongMessage::class.java

    override fun handle(message: PongMessage, tags: ITagStore) {
        LOGGER.debug("got pong with token ${message.token}")

        connectionState.lastPingOrPong = System.currentTimeMillis()
    }

}