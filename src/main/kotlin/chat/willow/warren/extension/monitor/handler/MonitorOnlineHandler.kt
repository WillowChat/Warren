package chat.willow.warren.extension.monitor.handler

import chat.willow.kale.IMetadataStore
import chat.willow.kale.KaleHandler
import chat.willow.kale.irc.message.extension.monitor.rpl.RplMonOnline
import chat.willow.warren.event.IWarrenEventDispatcher
import chat.willow.warren.extension.monitor.UserOnlineEvent
import chat.willow.warren.helper.loggerFor

class MonitorOnlineHandler(private val eventDispatcher: IWarrenEventDispatcher) : KaleHandler<RplMonOnline.Message>(RplMonOnline.Message.Parser) {

    private val LOGGER = loggerFor<MonitorOnlineHandler>()

    override fun handle(message: RplMonOnline.Message, metadata: IMetadataStore) {
        message.targets.forEach { eventDispatcher.fire(UserOnlineEvent(it)) }
    }

}