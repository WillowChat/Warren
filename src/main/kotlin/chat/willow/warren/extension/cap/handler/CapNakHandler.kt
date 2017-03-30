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

class CapNakHandler(val capState: CapState, val saslState: SaslState, val sink: IMessageSink, val capManager: ICapManager) : KaleHandler<CapMessage.Nak.Message>(CapMessage.Nak.Message.Parser) {

    private val LOGGER = loggerFor<CapNakHandler>()


    override fun handle(message: CapMessage.Nak.Message, metadata: IMetadataStore) {
        val caps = message.caps
        val lifecycle = capState.lifecycle

        LOGGER.trace("server NAKed following caps: $caps")

        for (cap in caps) {
            if (capState.rejected.contains(cap)) {
                LOGGER.debug("we've already rejected cap $cap")
                continue
            }

            capState.rejected += cap
            capManager.capDisabled(cap)
        }

        when (lifecycle) {
            CapLifecycle.NEGOTIATING -> {
                LOGGER.trace("server NAKed some caps, checked if it's the last reply")

                val remainingCaps = capState.negotiate.subtract(capState.accepted).subtract(capState.rejected)
                LOGGER.trace("remaining caps to negotiate: $remainingCaps")

                capManager.onRegistrationStateChanged()
            }

            else -> Unit
        }
    }

}

