package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rfc1459.PingMessage
import engineer.carrot.warren.kale.irc.message.rfc1459.PongMessage
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.loggerFor
import engineer.carrot.warren.warren.state.ConnectionState

class PingHandler(val sink: IMessageSink, val connectionState: ConnectionState) : IKaleHandler<PingMessage> {

    private val LOGGER = loggerFor<PingHandler>()

    override val messageType = PingMessage::class.java

    override fun handle(message: PingMessage, tags: Map<String, String?>) {
        LOGGER.debug("handling ping with token ${message.token}")

        sink.write(PongMessage(token = message.token))

        connectionState.lastPingOrPong = System.currentTimeMillis()
    }

}