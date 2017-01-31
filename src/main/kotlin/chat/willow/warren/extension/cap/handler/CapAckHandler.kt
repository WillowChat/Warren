package chat.willow.warren.extension.cap.handler

import chat.willow.kale.IKaleHandler
import chat.willow.kale.irc.message.extension.cap.CapAckMessage
import chat.willow.kale.irc.message.extension.sasl.AuthenticateMessage
import chat.willow.warren.IMessageSink
import chat.willow.warren.extension.cap.CapLifecycle
import chat.willow.warren.extension.cap.CapState
import chat.willow.warren.extension.cap.ICapManager
import chat.willow.warren.extension.sasl.SaslState
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.AuthLifecycle

class CapAckHandler(val capState: CapState, val saslState: SaslState, val sink: IMessageSink, val capManager: ICapManager) : IKaleHandler<CapAckMessage> {

    private val LOGGER = loggerFor<CapAckHandler>()

    override val messageType = CapAckMessage::class.java

    override fun handle(message: CapAckMessage, tags: Map<String, String?>) {
        val caps = message.caps
        val lifecycle = capState.lifecycle

        LOGGER.trace("server ACKed following caps: $caps")

        for (cap in caps) {
            if (!capState.negotiate.contains(cap)) {
                LOGGER.debug("server acked cap we don't think we asked for $cap")
                continue
            }

            if (capState.accepted.contains(cap)) {
                LOGGER.debug("we've already accepted cap $cap")
                continue
            }

            capState.rejected -= cap
            capState.accepted += cap
            capManager.capEnabled(cap)
        }

        when (lifecycle) {
            CapLifecycle.NEGOTIATING -> {
                LOGGER.trace("server ACKed some caps, checked if it's the last reply")

                if (caps.contains("sasl") && saslState.shouldAuth) {
                    LOGGER.trace("server acked sasl - starting authentication for user: ${saslState.credentials?.account}")

                    saslState.lifecycle = AuthLifecycle.AUTHING

                    sink.write(AuthenticateMessage(payload = "PLAIN", isEmpty = false))
                }

                capManager.onRegistrationStateChanged()
            }

            else -> Unit
        }
    }

}

