package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rfc1459.PingMessage
import engineer.carrot.warren.kale.irc.message.rfc1459.PongMessage
import engineer.carrot.warren.warren.IMessageSink

class PingHandler(val sink: IMessageSink): IKaleHandler<PingMessage> {
    override val messageType = PingMessage::class.java

    override fun handle(message: PingMessage) {
        println("handling ping with token ${message.token}")
        sink.write(PongMessage(token = message.token))
    }
}

