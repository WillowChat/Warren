package chat.willow.warren.extension.monitor.handler

import chat.willow.kale.IKaleHandler
import chat.willow.kale.irc.message.extension.monitor.rpl.RplMonOfflineMessage
import chat.willow.warren.helper.loggerFor

class MonitorOfflineHandler : IKaleHandler<RplMonOfflineMessage> {

    private val LOGGER = loggerFor<MonitorOnlineHandler>()
    override val messageType = RplMonOfflineMessage::class.java

    override fun handle(message: RplMonOfflineMessage, tags: Map<String, String?>) {
        LOGGER.debug("users offline: ${message.targets.map { it.nick }.joinToString() }")

        // go through targets
        // fire events for them being online/offline?
        //  or store something about them being online/offline in extension?
    }

}