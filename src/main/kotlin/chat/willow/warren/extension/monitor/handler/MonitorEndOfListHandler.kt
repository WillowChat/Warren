package chat.willow.warren.extension.monitor.handler

import chat.willow.kale.IMetadataStore
import chat.willow.kale.KaleHandler
import chat.willow.kale.irc.message.extension.monitor.rpl.RplEndOfMonList
import chat.willow.warren.helper.loggerFor

class MonitorEndOfListHandler : KaleHandler<RplEndOfMonList.Message>(RplEndOfMonList.Message.Parser) {

    private val LOGGER = loggerFor<MonitorOnlineHandler>()

    override fun handle(message: RplEndOfMonList.Message, metadata: IMetadataStore) {
        // ends current mon list
    }

}