package engineer.carrot.warren.warren.extension.extended_join

import engineer.carrot.warren.kale.IKale
import engineer.carrot.warren.kale.irc.message.rfc1459.JoinMessage
import engineer.carrot.warren.warren.extension.cap.ICapExtension
import engineer.carrot.warren.warren.state.CaseMappingState
import engineer.carrot.warren.warren.state.ChannelsState

class ExtendedJoinExtension(private val kale: IKale, private val channelsState: ChannelsState, private val caseMappingState: CaseMappingState) : ICapExtension {

    lateinit var handler: ExtendedJoinHandler

    override fun setUp() {
        val joinHandler = kale.handlerFor(JoinMessage::class.java) ?: return
        handler = ExtendedJoinHandler(joinHandler, channelsState, caseMappingState)

        kale.register(handler)
    }

    override fun tearDown() {
        kale.unregister(handler)
    }

}