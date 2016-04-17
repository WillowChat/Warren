package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rfc1459.JoinMessage
import engineer.carrot.warren.kale.irc.message.rpl.Rpl376Message
import engineer.carrot.warren.warren.IMessageSink

class Rpl376Handler(val sink: IMessageSink, val channelsToJoin: List<String>) : IKaleHandler<Rpl376Message> {
    override val messageType = Rpl376Message::class.java

    override fun handle(message: Rpl376Message) {
        println("got end of MOTD, joining channels")
        sink.write(JoinMessage(channels = channelsToJoin))
    }
}

