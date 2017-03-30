package chat.willow.warren.extension.away_notify

import chat.willow.kale.IKaleIrcMessageHandler
import chat.willow.kale.IKaleRouter
import chat.willow.kale.irc.message.extension.away_notify.AwayMessage
import chat.willow.warren.extension.cap.ICapExtension
import chat.willow.warren.state.JoinedChannelsState

class AwayNotifyExtension(private val kaleRouter: IKaleRouter<IKaleIrcMessageHandler>, private val channelsState: JoinedChannelsState) : ICapExtension {

    val handler: AwayHandler by lazy { AwayHandler(channelsState) }

    override fun setUp() {
        kaleRouter.register(AwayMessage.command, handler)
    }

    override fun tearDown() {
        kaleRouter.unregister(AwayMessage.command)
    }

}