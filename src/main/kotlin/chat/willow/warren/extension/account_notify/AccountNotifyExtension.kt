package chat.willow.warren.extension.account_notify

import chat.willow.kale.IKale
import chat.willow.warren.extension.cap.ICapExtension
import chat.willow.warren.state.JoinedChannelsState

class AccountNotifyExtension(private val kale: IKale, private val channelsState: JoinedChannelsState) : ICapExtension {

    val handler: AccountHandler by lazy { AccountHandler(channelsState) }

    override fun setUp() {
        kale.register(handler)
    }

    override fun tearDown() {
        kale.unregister(handler)
    }

}