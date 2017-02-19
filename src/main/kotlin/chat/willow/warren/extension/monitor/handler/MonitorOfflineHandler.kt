package chat.willow.warren.extension.monitor.handler

import chat.willow.kale.IKaleHandler
import chat.willow.kale.irc.message.extension.monitor.rpl.RplMonOfflineMessage
import chat.willow.kale.irc.tag.ITagStore
import chat.willow.warren.event.IWarrenEventDispatcher
import chat.willow.warren.extension.monitor.UserOfflineEvent
import chat.willow.warren.helper.loggerFor

class MonitorOfflineHandler(private val eventDispatcher: IWarrenEventDispatcher) : IKaleHandler<RplMonOfflineMessage> {

    private val LOGGER = loggerFor<MonitorOnlineHandler>()
    override val messageType = RplMonOfflineMessage::class.java

    override fun handle(message: RplMonOfflineMessage, tags: ITagStore) {
        message.targets.forEach { eventDispatcher.fire(UserOfflineEvent(it)) }
    }

}