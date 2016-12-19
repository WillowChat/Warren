package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.extension.cap.CapNakMessage
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.extension.cap.CapLifecycle
import engineer.carrot.warren.warren.extension.cap.CapState
import engineer.carrot.warren.warren.extension.cap.ICapManager
import engineer.carrot.warren.warren.extension.sasl.SaslState
import engineer.carrot.warren.warren.loggerFor

class CapNakHandler(val capState: CapState, val saslState: SaslState, val sink: IMessageSink, val capManager: ICapManager) : IKaleHandler<CapNakMessage> {

    private val LOGGER = loggerFor<CapNakHandler>()

    override val messageType = CapNakMessage::class.java

    override fun handle(message: CapNakMessage, tags: Map<String, String?>) {
        val caps = message.caps
        val lifecycle = capState.lifecycle

        LOGGER.trace("server NAKed following caps: $caps")

        capState.rejected += caps
        caps.forEach { capManager.capDisabled(it) }

        when (lifecycle) {
            CapLifecycle.NEGOTIATING -> {
                LOGGER.trace("server NAKed some caps, checked if it's the last reply")

                val remainingCaps = capState.negotiate.subtract(capState.accepted).subtract(capState.rejected)
                LOGGER.trace("remaining caps to negotiate: $remainingCaps")

                capManager.onRegistrationStateChanged()
            }

            else -> LOGGER.trace("server NAKed caps but we don't think we're negotiating")
        }
    }

}

