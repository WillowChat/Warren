package chat.willow.warren.extension.away_notify

import chat.willow.kale.IKale
import chat.willow.warren.extension.cap.ICapExtension
import chat.willow.warren.state.JoinedChannelsState

class AwayNotifyExtension(private val kale: IKale, private val channelsState: JoinedChannelsState) : ICapExtension {

    val handler: AwayHandler by lazy { AwayHandler(channelsState) }

    override fun setUp() {
        kale.register(handler)
    }

    override fun tearDown() {
        kale.unregister(handler)
    }

}