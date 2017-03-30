package chat.willow.warren.extension.monitor.handler

import chat.willow.kale.IMetadataStore
import chat.willow.kale.KaleHandler
import chat.willow.kale.irc.message.extension.monitor.rpl.RplMonList
import chat.willow.warren.helper.loggerFor

class MonitorListHandler : KaleHandler<RplMonList.Message>(RplMonList.Message.Parser) {

    private val LOGGER = loggerFor<MonitorOnlineHandler>()

    override fun handle(message: RplMonList.Message, metadata: IMetadataStore) {
        // adds to current listing until Rpl_EndOfMonList
    }

}