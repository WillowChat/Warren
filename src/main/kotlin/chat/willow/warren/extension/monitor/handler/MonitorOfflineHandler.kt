package chat.willow.warren.extension.monitor.handler

import chat.willow.kale.IMetadataStore
import chat.willow.kale.KaleHandler
import chat.willow.kale.irc.message.extension.monitor.rpl.RplMonOffline
import chat.willow.warren.event.IWarrenEventDispatcher
import chat.willow.warren.extension.monitor.UserOfflineEvent
import chat.willow.warren.helper.loggerFor

class MonitorOfflineHandler(private val eventDispatcher: IWarrenEventDispatcher) : KaleHandler<RplMonOffline.Message>(RplMonOffline.Message.Parser) {

    private val LOGGER = loggerFor<MonitorOnlineHandler>()

    override fun handle(message: RplMonOffline.Message, metadata: IMetadataStore) {
        message.targets.forEach { eventDispatcher.fire(UserOfflineEvent(it)) }
    }

}