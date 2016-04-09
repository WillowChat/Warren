package engineer.carrot.warren.warren

import engineer.carrot.warren.kale.IKale
import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rfc1459.*
import engineer.carrot.warren.kale.irc.message.rpl.Rpl376Message

class IrcRunner(val connectionInfo: ConnectionInfo, val kale: IKale, val sink: IMessageSink, val processor: IMessageProcessor) {

    fun run() {
        kale.register(PingHandler(sink))
        kale.register(Rpl376Handler(sink))

        sink.writeMessage(NickMessage(nickname = connectionInfo.nickname))
        sink.writeMessage(UserMessage(username = connectionInfo.nickname, mode = "8", realname = connectionInfo.nickname))

        do {
            val processed = processor.processNextMessage()

            if (!processed) {
                println("processing message returned false, bailing")
                return
            }
        } while (true)
    }
}

class PingHandler(val sink: IMessageSink): IKaleHandler<PingMessage> {
    override val messageType = PingMessage::class.java

    override fun handle(message: PingMessage) {
        println("handling ping with token ${message.token}")
        sink.writeMessage(PongMessage(token = message.token))
    }
}

class Rpl376Handler(val sink: IMessageSink): IKaleHandler<Rpl376Message> {
    override val messageType = Rpl376Message::class.java

    override fun handle(message: Rpl376Message) {
        println("got end of MOTD, joining channels")
        sink.writeMessage(JoinMessage(channels = listOf("#compsoc")))
    }
}