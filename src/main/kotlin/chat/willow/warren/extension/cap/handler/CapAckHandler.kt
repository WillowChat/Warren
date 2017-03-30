package chat.willow.warren.extension.cap.handler

import chat.willow.kale.IMetadataStore
import chat.willow.kale.KaleHandler
import chat.willow.kale.irc.message.extension.cap.CapMessage
import chat.willow.warren.IMessageSink
import chat.willow.warren.extension.cap.CapLifecycle
import chat.willow.warren.extension.cap.CapState
import chat.willow.warren.extension.cap.ICapManager
import chat.willow.warren.extension.sasl.SaslState
import chat.willow.warren.helper.loggerFor

class CapAckHandler(val capState: CapState,
                    val saslState: SaslState,
                    val sink: IMessageSink,
                    val capManager: ICapManager) : KaleHandler<CapMessage.Ack.Message>(CapMessage.Ack.Message.Parser) {

    private val LOGGER = loggerFor<CapAckHandler>()


    override fun handle(message: CapMessage.Ack.Message, metadata: IMetadataStore) {
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

        if (lifecycle == CapLifecycle.NEGOTIATING) {
            capManager.onRegistrationStateChanged()
        }
    }

}

