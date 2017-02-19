package chat.willow.warren.extension.cap.handler

import chat.willow.kale.IKaleHandler
import chat.willow.kale.irc.message.extension.cap.CapDelMessage
import chat.willow.kale.irc.tag.ITagStore
import chat.willow.warren.IMessageSink
import chat.willow.warren.extension.cap.CapState
import chat.willow.warren.extension.cap.ICapManager
import chat.willow.warren.helper.loggerFor

class CapDelHandler(val capState: CapState, val sink: IMessageSink, val capManager: ICapManager) : IKaleHandler<CapDelMessage> {

    private val LOGGER = loggerFor<CapDelHandler>()

    override val messageType = CapDelMessage::class.java

    override fun handle(message: CapDelMessage, tags: ITagStore) {
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

