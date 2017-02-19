package chat.willow.warren.extension.monitor.handler

import chat.willow.kale.IKaleHandler
import chat.willow.kale.irc.message.extension.monitor.rpl.RplMonListMessage
import chat.willow.kale.irc.tag.ITagStore
import chat.willow.warren.helper.loggerFor

class MonitorListHandler : IKaleHandler<RplMonListMessage> {

    private val LOGGER = loggerFor<MonitorOnlineHandler>()
    override val messageType = RplMonListMessage::class.java

    override fun handle(message: RplMonListMessage, tags: ITagStore) {
        // adds to current listing until Rpl_EndOfMonList
    }

}