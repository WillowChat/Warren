package chat.willow.warren.extension.monitor.handler

import chat.willow.kale.IKaleHandler
import chat.willow.kale.irc.message.extension.monitor.rpl.RplEndOfMonListMessage
import chat.willow.warren.helper.loggerFor

class MonitorEndOfListHandler : IKaleHandler<RplEndOfMonListMessage> {

    private val LOGGER = loggerFor<MonitorOnlineHandler>()
    override val messageType = RplEndOfMonListMessage::class.java

    override fun handle(message: RplEndOfMonListMessage, tags: Map<String, String?>) {
        // ends current mon list
    }

}