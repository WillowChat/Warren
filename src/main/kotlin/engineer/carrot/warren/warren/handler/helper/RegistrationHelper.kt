package engineer.carrot.warren.warren.handler.helper

import engineer.carrot.warren.kale.irc.message.ircv3.CapEndMessage
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.loggerFor
import engineer.carrot.warren.warren.state.AuthLifecycle
import engineer.carrot.warren.warren.state.CapLifecycle
import engineer.carrot.warren.warren.state.CapState
import engineer.carrot.warren.warren.state.SaslState

object RegistrationHelper {

    private val LOGGER = loggerFor<RegistrationHelper>()

    fun shouldEndCapNegotiation(saslState: SaslState, capState: CapState): Boolean {
        val remainingCaps = capState.negotiate.subtract(capState.accepted).subtract(capState.rejected)
        LOGGER.trace("cap end checker: remaining caps to negotiate: $remainingCaps")

        if (remainingCaps.isEmpty()) {
            if (saslState.lifecycle == AuthLifecycle.AUTHING) {
                LOGGER.debug("cap end checker: no more remaining caps, but we're still authenticating")
            } else {
                LOGGER.debug("cap end checker: no more remaining caps, SASL not authing - good to end negotiation")

                return true
            }
        }

        return false
    }

    fun endCapNegotiation(sink: IMessageSink, capState: CapState) {
        capState.lifecycle = CapLifecycle.NEGOTIATED

        LOGGER.debug("ending cap negotiation with state: $capState")

        sink.write(CapEndMessage())
    }

}