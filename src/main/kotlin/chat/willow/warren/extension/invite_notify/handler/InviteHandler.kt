package chat.willow.warren.extension.invite_notify.handler

import chat.willow.kale.IKaleHandler
import chat.willow.kale.irc.message.rfc1459.InviteMessage
import chat.willow.kale.irc.tag.ITagStore
import chat.willow.warren.event.IWarrenEventDispatcher
import chat.willow.warren.event.InvitedToChannelEvent
import chat.willow.warren.helper.loggerFor

class InviteHandler(val eventDispatcher: IWarrenEventDispatcher) : IKaleHandler<InviteMessage> {

    private val LOGGER = loggerFor<InviteHandler>()
    override val messageType = InviteMessage::class.java

    override fun handle(message: InviteMessage, tags: ITagStore) {
        val source = message.source
        if (source == null) {
            LOGGER.debug("got an invite with a missing source, ignoring")
            return
        }

        eventDispatcher.fire(InvitedToChannelEvent(source = source, channel = message.channel, metadata = tags))
    }

}