package chat.willow.warren.extension.monitor.handler

import chat.willow.kale.IKaleHandler
import chat.willow.kale.irc.message.extension.monitor.rpl.RplMonOnlineMessage
import chat.willow.warren.event.IWarrenEventDispatcher
import chat.willow.warren.extension.monitor.UserOnlineEvent
import chat.willow.warren.helper.loggerFor

class MonitorOnlineHandler(private val eventDispatcher: IWarrenEventDispatcher) : IKaleHandler<RplMonOnlineMessage> {

    private val LOGGER = loggerFor<MonitorOnlineHandler>()
    override val messageType = RplMonOnlineMessage::class.java

    override fun handle(message: RplMonOnlineMessage, tags: Map<String, String?>) {
        message.targets.forEach { eventDispatcher.fire(UserOnlineEvent(it)) }
    }

}