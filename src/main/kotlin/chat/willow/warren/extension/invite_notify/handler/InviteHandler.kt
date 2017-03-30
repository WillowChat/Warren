package chat.willow.warren.extension.invite_notify.handler

import chat.willow.kale.IMetadataStore
import chat.willow.kale.KaleHandler
import chat.willow.kale.irc.message.rfc1459.InviteMessage
import chat.willow.warren.event.IWarrenEventDispatcher
import chat.willow.warren.event.InvitedToChannelEvent
import chat.willow.warren.helper.loggerFor

class InviteHandler(val eventDispatcher: IWarrenEventDispatcher) : KaleHandler<InviteMessage.Message>(InviteMessage.Message.Parser) {

    private val LOGGER = loggerFor<InviteHandler>()

    override fun handle(message: InviteMessage.Message, metadata: IMetadataStore) {
        val source = message.source

        eventDispatcher.fire(InvitedToChannelEvent(source = source, channel = message.channel, metadata = metadata))
    }

}