package chat.willow.warren.extension.cap.handler

import chat.willow.kale.IMetadataStore
import chat.willow.kale.KaleHandler
import chat.willow.kale.irc.message.extension.cap.CapMessage
import chat.willow.warren.IMessageSink
import chat.willow.warren.extension.cap.CapLifecycle
import chat.willow.warren.extension.cap.CapState
import chat.willow.warren.extension.cap.ICapManager
import chat.willow.warren.helper.loggerFor

class CapLsHandler(val capState: CapState, val sink: IMessageSink, val capManager: ICapManager) : KaleHandler<CapMessage.Ls.Message>(CapMessage.Ls.Message.Parser) {

    private val LOGGER = loggerFor<CapLsHandler>()


    override fun handle(message: CapMessage.Ls.Message, metadata: IMetadataStore) {
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

                        sink.write(CapMessage.Req.Command(caps = requestCaps.distinct()))
                    }
                } else {
                    LOGGER.trace("server gave us a multiline cap ls, expecting more caps before ending")
                }

                message.caps.forEach { capManager.capValueSet(it.key, it.value) }
            }

            else -> LOGGER.trace("server told us about caps but we don't think we're negotiating")
        }
    }

}

