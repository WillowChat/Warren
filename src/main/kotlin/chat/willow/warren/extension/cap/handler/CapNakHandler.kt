package chat.willow.warren.extension.cap.handler

import chat.willow.kale.IKaleHandler
import chat.willow.kale.irc.message.extension.cap.CapNakMessage
import chat.willow.warren.IMessageSink
import chat.willow.warren.extension.cap.CapLifecycle
import chat.willow.warren.extension.cap.CapState
import chat.willow.warren.extension.cap.ICapManager
import chat.willow.warren.extension.sasl.SaslState
import chat.willow.warren.helper.loggerFor

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

