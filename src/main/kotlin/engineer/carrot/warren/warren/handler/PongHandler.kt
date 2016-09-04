package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rfc1459.PongMessage
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.loggerFor
import engineer.carrot.warren.warren.state.ConnectionState

class PongHandler(val sink: IMessageSink, val connectionState: ConnectionState) : IKaleHandler<PongMessage> {

    private val LOGGER = loggerFor<PongHandler>()

    override val messageType = PongMessage::class.java

    override fun handle(message: PongMessage, tags: Map<String, String?>) {
        LOGGER.debug("got pong with token ${message.token}")

        connectionState.lastPingOrPong = System.currentTimeMillis()
    }

}