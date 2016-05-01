package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.ircv3.CapNakMessage
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.handler.helper.RegistrationHelper
import engineer.carrot.warren.warren.state.CapLifecycle
import engineer.carrot.warren.warren.state.CapState
import engineer.carrot.warren.warren.state.SaslState

class CapNakHandler(val capState: CapState, val saslState: SaslState, val sink: IMessageSink) : IKaleHandler<CapNakMessage> {
    override val messageType = CapNakMessage::class.java

    override fun handle(message: CapNakMessage) {
        val caps = message.caps
        val lifecycle = capState.lifecycle

        println("server NAKed following caps: $caps")

        capState.rejected += caps

        when(lifecycle) {
            CapLifecycle.NEGOTIATING -> {
                println("server NAKed some caps, checked if it's the last reply")

                val remainingCaps = capState.negotiate.subtract(capState.accepted).subtract(capState.rejected)
                println("remaining caps to negotiate: $remainingCaps")

                if (RegistrationHelper.shouldEndCapNegotiation(saslState, capState)) {
                    RegistrationHelper.endCapNegotiation(sink, capState)
                } else {
                    println("didn't think we should end the registration process, waiting")
                }
            }

            else -> println("server NAKed caps but we don't think we're negotiating")
        }
    }
}

