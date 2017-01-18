package chat.willow.warren.extension.extended_join

import chat.willow.kale.IKale
import chat.willow.kale.irc.message.rfc1459.JoinMessage
import chat.willow.warren.extension.cap.ICapExtension
import chat.willow.warren.state.CaseMappingState
import chat.willow.warren.state.ChannelsState

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