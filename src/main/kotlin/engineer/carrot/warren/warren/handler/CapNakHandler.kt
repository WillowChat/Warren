package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.ircv3.CapNakMessage
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.handler.helper.RegistrationHelper
import engineer.carrot.warren.warren.loggerFor
import engineer.carrot.warren.warren.state.CapLifecycle
import engineer.carrot.warren.warren.state.CapState
import engineer.carrot.warren.warren.state.SaslState

class CapNakHandler(val capState: CapState, val saslState: SaslState, val sink: IMessageSink) : IKaleHandler<CapNakMessage> {
    private val LOGGER = loggerFor<CapNakHandler>()

    override val messageType = CapNakMessage::class.java

    override fun handle(message: CapNakMessage, tags: Map<String, String?>) {
        val caps = message.caps
        val lifecycle = capState.lifecycle

        LOGGER.trace("server NAKed following caps: $caps")

        capState.rejected += caps

        when (lifecycle) {
            CapLifecycle.NEGOTIATING -> {
                LOGGER.trace("server NAKed some caps, checked if it's the last reply")

                val remainingCaps = capState.negotiate.subtract(capState.accepted).subtract(capState.rejected)
                LOGGER.trace("remaining caps to negotiate: $remainingCaps")

                if (RegistrationHelper.shouldEndCapNegotiation(saslState, capState)) {
                    RegistrationHelper.endCapNegotiation(sink, capState)
                } else {
                    LOGGER.trace("didn't think we should end the registration process, waiting")
                }
            }

            else -> LOGGER.trace("server NAKed caps but we don't think we're negotiating")
        }
    }
}

