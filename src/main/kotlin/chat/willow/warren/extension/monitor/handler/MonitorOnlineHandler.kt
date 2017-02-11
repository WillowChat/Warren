package chat.willow.warren.extension.monitor.handler

import chat.willow.kale.IKaleHandler
import chat.willow.kale.irc.message.extension.monitor.rpl.RplMonOnlineMessage
import chat.willow.warren.helper.loggerFor

class MonitorOnlineHandler : IKaleHandler<RplMonOnlineMessage> {

    private val LOGGER = loggerFor<MonitorOnlineHandler>()
    override val messageType = RplMonOnlineMessage::class.java

    override fun handle(message: RplMonOnlineMessage, tags: Map<String, String?>) {
        LOGGER.debug("users online: ${message.targets.map { it.nick }.joinToString() }")

        // go through targets
        // fire events for them being online/offline?
        //  or store something about them being online/offline in extension?
    }

}