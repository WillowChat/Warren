package engineer.carrot.warren.warren.handler.sasl

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.ircv3.sasl.Rpl903Message
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.handler.helper.RegistrationHelper
import engineer.carrot.warren.warren.state.*

class Rpl903Handler(val capState: CapState, val saslState: SaslState, val sink: IMessageSink) : IKaleHandler<Rpl903Message> {
    override val messageType = Rpl903Message::class.java

    override fun handle(message: Rpl903Message) {
        println("sasl auth successful for user: ${saslState.credentials?.account}")

        saslState.lifecycle = SaslLifecycle.AUTHED

        if (RegistrationHelper.shouldEndCapNegotiation(saslState, capState)) {
            RegistrationHelper.endCapNegotiation(sink, capState)
        } else {
            println("didn't think we should end the registration process, waiting")
        }
    }
}

