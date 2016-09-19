package engineer.carrot.warren.warren.extension.away_notify

import engineer.carrot.warren.kale.IKale
import engineer.carrot.warren.warren.extension.cap.ICapExtension
import engineer.carrot.warren.warren.state.JoinedChannelsState

class AwayNotifyExtension(private val kale: IKale, private val channelsState: JoinedChannelsState) : ICapExtension {

    val handler: AwayHandler by lazy { AwayHandler(channelsState) }

    override fun setUp() {
        kale.register(handler)
    }

    override fun tearDown() {
        kale.unregister(handler)
    }

}