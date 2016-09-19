package engineer.carrot.warren.warren.extension.account_notify

import engineer.carrot.warren.kale.IKale
import engineer.carrot.warren.warren.extension.cap.ICapExtension
import engineer.carrot.warren.warren.state.JoinedChannelsState

class AccountNotifyExtension(private val kale: IKale, private val channelsState: JoinedChannelsState) : ICapExtension {

    val handler: AccountHandler by lazy { AccountHandler(channelsState) }

    override fun setUp() {
        kale.register(handler)
    }

    override fun tearDown() {
        kale.unregister(handler)
    }

}