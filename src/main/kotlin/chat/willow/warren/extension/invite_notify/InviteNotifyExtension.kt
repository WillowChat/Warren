package chat.willow.warren.extension.invite_notify

import chat.willow.kale.IKaleIrcMessageHandler
import chat.willow.kale.IKaleRouter
import chat.willow.kale.irc.message.rfc1459.InviteMessage
import chat.willow.warren.event.IWarrenEventDispatcher
import chat.willow.warren.extension.cap.ICapExtension
import chat.willow.warren.extension.invite_notify.handler.InviteHandler

class InviteNotifyExtension(private val kaleRouter: IKaleRouter<IKaleIrcMessageHandler>, private val eventDispatcher: IWarrenEventDispatcher) : ICapExtension {

    val handler: InviteHandler by lazy { InviteHandler(eventDispatcher) }

    override fun setUp() {
        kaleRouter.register(InviteMessage.command, handler)
    }

    override fun tearDown() {
        kaleRouter.unregister(InviteMessage.command)
    }

}