package chat.willow.warren.extension.cap.handler

import chat.willow.kale.IMetadataStore
import chat.willow.kale.KaleHandler
import chat.willow.kale.irc.message.extension.cap.CapMessage
import chat.willow.warren.IMessageSink
import chat.willow.warren.extension.cap.CapState
import chat.willow.warren.extension.cap.ICapManager
import chat.willow.warren.helper.loggerFor

class CapDelHandler(val capState: CapState, val sink: IMessageSink, val capManager: ICapManager) : KaleHandler<CapMessage.Del.Message>(CapMessage.Del.Message.Parser) {

    private val LOGGER = loggerFor<CapDelHandler>()


    override fun handle(message: CapMessage.Del.Message, metadata: IMetadataStore) {
        val caps = message.caps

        LOGGER.trace("server NEWed following caps: $caps")

        for (cap in caps) {
            if (capState.rejected.contains(cap)) {
                LOGGER.debug("we've already rejected cap $cap")
                continue
            }

            if (!capState.accepted.contains(cap)) {
                LOGGER.debug("cap is available but we didn't enable it, ignoring $cap")
                continue
            }

            capState.rejected += cap
            capState.accepted -= cap

            capManager.capDisabled(cap)
        }
    }

}

