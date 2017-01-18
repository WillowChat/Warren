package chat.willow.warren.handler

import chat.willow.kale.IKaleHandler
import chat.willow.kale.irc.message.extension.cap.CapLsMessage
import chat.willow.kale.irc.message.extension.cap.CapReqMessage
import chat.willow.warren.IMessageSink
import chat.willow.warren.extension.cap.CapLifecycle
import chat.willow.warren.extension.cap.CapState
import chat.willow.warren.extension.cap.ICapManager
import chat.willow.warren.extension.sasl.SaslState
import chat.willow.warren.helper.loggerFor

class CapLsHandler(val capState: CapState, val saslState: SaslState, val sink: IMessageSink, val capManager: ICapManager) : IKaleHandler<CapLsMessage> {

    private val LOGGER = loggerFor<CapLsHandler>()

    override val messageType = CapLsMessage::class.java

    override fun handle(message: CapLsMessage, tags: Map<String, String?>) {
        val caps = message.caps
        val lifecycle = capState.lifecycle

        capState.server += message.caps

        LOGGER.trace("server supports following caps: $caps")

        when (lifecycle) {
            CapLifecycle.NEGOTIATING -> {
                if (!message.isMultiline) {
                    val requestCaps = capState.server.keys.intersect(capState.negotiate)
                    val implicitlyRejectedCaps = capState.negotiate.subtract(requestCaps)

                    capState.rejected += implicitlyRejectedCaps

                    capManager.onRegistrationStateChanged()

                    if (!requestCaps.isEmpty()) {
                        LOGGER.trace("server gave us caps and ended with a non-multiline ls, requesting: $requestCaps, implicitly rejecting: $implicitlyRejectedCaps")

                        sink.write(CapReqMessage(caps = requestCaps.distinct()))
                    }
                } else {
                    LOGGER.trace("server gave us a multiline cap ls, expecting more caps before ending")
                }
            }

            else -> LOGGER.trace("server told us about caps but we don't think we're negotiating")
        }
    }

}

