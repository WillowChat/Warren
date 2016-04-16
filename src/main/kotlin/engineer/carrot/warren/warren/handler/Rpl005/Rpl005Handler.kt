package engineer.carrot.warren.warren.handler.Rpl005

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rfc1459.JoinMessage
import engineer.carrot.warren.kale.irc.message.rpl.Rpl005Message
import engineer.carrot.warren.warren.IMessageSink

import engineer.carrot.warren.warren.state.ParsingState

class Rpl005Handler(val state: ParsingState, val prefixHandler: IRpl005PrefixHandler): IKaleHandler<Rpl005Message> {
    override val messageType = Rpl005Message::class.java

    override fun handle(message: Rpl005Message) {
        println("got isupport additions: ${message.tokens}")

        for ((key, value) in message.tokens) {
            if (key == "PREFIX" && value != null) {
                prefixHandler.handle(value, state.userPrefixes)
            }
        }
    }
}