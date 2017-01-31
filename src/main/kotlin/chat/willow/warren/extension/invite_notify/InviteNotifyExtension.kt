package chat.willow.warren.extension.invite_notify

import chat.willow.kale.IKale
import chat.willow.warren.event.IWarrenEventDispatcher
import chat.willow.warren.extension.cap.ICapExtension
import chat.willow.warren.extension.invite_notify.handler.InviteHandler

class InviteNotifyExtension(private val kale: IKale, private val eventDispatcher: IWarrenEventDispatcher) : ICapExtension {

    val handler: InviteHandler by lazy { InviteHandler(eventDispatcher) }

    override fun setUp() {
        kale.register(handler)
    }

    override fun tearDown() {
        kale.unregister(handler)
    }

}