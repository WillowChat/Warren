package chat.willow.warren.extension.monitor.handler

import chat.willow.kale.IMetadataStore
import chat.willow.kale.KaleHandler
import chat.willow.kale.irc.message.extension.monitor.rpl.RplMonListIsFull
import chat.willow.warren.helper.loggerFor

class MonitorListFullHandler : KaleHandler<RplMonListIsFull.Message>(RplMonListIsFull.Message.Parser) {

    private val LOGGER = loggerFor<MonitorOnlineHandler>()

    override fun handle(message: RplMonListIsFull.Message, metadata: IMetadataStore) {
        // notifies that current list is full, with limit, and list of targets that couldn't be added
    }

}