package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.ircv3.CapAckMessage
import engineer.carrot.warren.kale.irc.message.ircv3.sasl.AuthenticateMessage
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.handler.helper.RegistrationHelper
import engineer.carrot.warren.warren.state.*

class CapAckHandler(val capState: CapState, val saslState: SaslState, val sink: IMessageSink) : IKaleHandler<CapAckMessage> {
    override val messageType = CapAckMessage::class.java

    override fun handle(message: CapAckMessage) {
        val caps = message.caps
        val lifecycle = capState.lifecycle

        println("server ACKed following caps: $caps")

        capState.accepted += caps

        if (caps.contains("sasl") && saslState.shouldAuth) {
            println("server acked sasl - starting authentication for user: ${saslState.credentials?.account}")

            saslState.lifecycle = SaslLifecycle.AUTHING

            sink.write(AuthenticateMessage(payload = "PLAIN", isEmpty = false))
        }

        when(lifecycle) {
            CapLifecycle.NEGOTIATING -> {
                println("server ACKed some caps, checked if it's the last reply")

                if (RegistrationHelper.shouldEndCapNegotiation(saslState, capState)) {
                    RegistrationHelper.endCapNegotiation(sink, capState)
                } else {
                    println("didn't think we should end the registration process, waiting")
                }
            }

            else -> println("server ACKed caps but we don't think we're negotiating")
        }
    }
}

