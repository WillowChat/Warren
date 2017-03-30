package chat.willow.warren.extension.account_notify

import chat.willow.kale.IKaleIrcMessageHandler
import chat.willow.kale.IKaleRouter
import chat.willow.kale.irc.message.extension.account_notify.AccountMessage
import chat.willow.warren.extension.cap.ICapExtension
import chat.willow.warren.state.JoinedChannelsState

class AccountNotifyExtension(private val kaleRouter: IKaleRouter<IKaleIrcMessageHandler>, private val channelsState: JoinedChannelsState) : ICapExtension {

    val handler: AccountHandler by lazy { AccountHandler(channelsState) }

    override fun setUp() {
        kaleRouter.register(AccountMessage.command, handler)
    }

    override fun tearDown() {
        kaleRouter.unregister(AccountMessage.command)
    }

}