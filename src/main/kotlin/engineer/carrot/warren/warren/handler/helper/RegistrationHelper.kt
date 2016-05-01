package engineer.carrot.warren.warren.handler.helper

import engineer.carrot.warren.kale.irc.message.ircv3.CapEndMessage
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.state.CapLifecycle
import engineer.carrot.warren.warren.state.CapState
import engineer.carrot.warren.warren.state.SaslLifecycle
import engineer.carrot.warren.warren.state.SaslState

object RegistrationHelper {
    fun shouldEndCapNegotiation(saslState: SaslState, capState: CapState): Boolean {
        val remainingCaps = capState.negotiate.subtract(capState.accepted).subtract(capState.rejected)
        println("cap end checker: remaining caps to negotiate: $remainingCaps")

        if (remainingCaps.isEmpty()) {
            if (saslState.lifecycle == SaslLifecycle.AUTHING) {
                println("cap end checker: no more remaining caps, but we're still authenticating")
            } else {
                println("cap end checker: no more remaining caps, SASL not authing - good to end negotiation")

                return true
            }
        }

        return false
    }

    fun endCapNegotiation(sink: IMessageSink, capState: CapState) {
        capState.lifecycle = CapLifecycle.NEGOTIATED

        println("ending cap negotiation with state: $capState")

        sink.write(CapEndMessage())
    }
}