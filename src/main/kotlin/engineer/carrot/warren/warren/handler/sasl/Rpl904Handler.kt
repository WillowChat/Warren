package engineer.carrot.warren.warren.handler.sasl

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.ircv3.sasl.Rpl904Message
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.handler.helper.RegistrationHelper
import engineer.carrot.warren.warren.state.*

class Rpl904Handler(val capState: CapState, val saslState: SaslState, val sink: IMessageSink) : IKaleHandler<Rpl904Message> {
    override val messageType = Rpl904Message::class.java

    override fun handle(message: Rpl904Message) {
        println("invalid mechanism, or sasl auth failed: ${message.contents}")

        saslState.lifecycle = SaslLifecycle.AUTH_FAILED

        if (RegistrationHelper.shouldEndCapNegotiation(saslState, capState)) {
            RegistrationHelper.endCapNegotiation(sink, capState)
        } else {
            println("didn't think we should end the registration process, waiting")
        }
    }
}

