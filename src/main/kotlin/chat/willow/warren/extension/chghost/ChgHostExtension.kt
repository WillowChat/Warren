package chat.willow.warren.extension.chghost

import chat.willow.kale.IKale
import chat.willow.warren.extension.cap.ICapExtension
import chat.willow.warren.state.JoinedChannelsState

class ChgHostExtension(private val kale: IKale, private val channelsState: JoinedChannelsState) : ICapExtension {

    val handler: ChgHostHandler by lazy { ChgHostHandler(channelsState) }

    override fun setUp() {
        kale.register(handler)
    }

    override fun tearDown() {
        kale.unregister(handler)
    }

}