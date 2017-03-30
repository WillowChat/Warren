package chat.willow.warren.extension.chghost

import chat.willow.kale.IKaleIrcMessageHandler
import chat.willow.kale.IKaleRouter
import chat.willow.kale.irc.message.extension.chghost.ChgHostMessage
import chat.willow.warren.extension.cap.ICapExtension
import chat.willow.warren.state.JoinedChannelsState

class ChgHostExtension(private val kaleRouter: IKaleRouter<IKaleIrcMessageHandler>, private val channelsState: JoinedChannelsState) : ICapExtension {

    val handler: ChgHostHandler by lazy { ChgHostHandler(channelsState) }

    override fun setUp() {
        kaleRouter.register(ChgHostMessage.command, handler)
    }

    override fun tearDown() {
        kaleRouter.unregister(ChgHostMessage.command)
    }

}