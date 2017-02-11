package chat.willow.warren.extension.monitor.handler

import chat.willow.kale.IKaleHandler
import chat.willow.kale.irc.message.extension.monitor.rpl.RplMonListIsFullMessage
import chat.willow.warren.helper.loggerFor

class MonitorListFullHandler : IKaleHandler<RplMonListIsFullMessage> {

    private val LOGGER = loggerFor<MonitorOnlineHandler>()
    override val messageType = RplMonListIsFullMessage::class.java

    override fun handle(message: RplMonListIsFullMessage, tags: Map<String, String?>) {
        // notifies that current list is full, with limit, and list of targets that couldn't be added
    }

}