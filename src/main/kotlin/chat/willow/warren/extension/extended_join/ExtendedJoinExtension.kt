package chat.willow.warren.extension.extended_join

import chat.willow.kale.IKaleIrcMessageHandler
import chat.willow.kale.IKaleMessageHandler
import chat.willow.kale.IKaleRouter
import chat.willow.kale.irc.message.extension.extended_join.ExtendedJoinMessage
import chat.willow.kale.irc.message.rfc1459.JoinMessage
import chat.willow.warren.extension.cap.ICapExtension
import chat.willow.warren.state.CaseMappingState
import chat.willow.warren.state.ChannelsState

class ExtendedJoinExtension(private val kaleRouter: IKaleRouter<IKaleIrcMessageHandler>, private val channelsState: ChannelsState, private val caseMappingState: CaseMappingState) : ICapExtension {

    lateinit var handler: ExtendedJoinHandler
    private var originalJoinHandler: IKaleIrcMessageHandler? = null

    override fun setUp() {
        originalJoinHandler = kaleRouter.handlerFor(JoinMessage.command)

        @Suppress("UNCHECKED_CAST")
        val joinMessageHandler = originalJoinHandler as? IKaleMessageHandler<JoinMessage.Message>

        handler = ExtendedJoinHandler(joinMessageHandler, channelsState, caseMappingState)

        kaleRouter.register(ExtendedJoinMessage.command, handler)
    }

    override fun tearDown() {
        kaleRouter.unregister(ExtendedJoinMessage.command)

        if (originalJoinHandler != null) {
            kaleRouter.register(JoinMessage.command, originalJoinHandler!!)
        }
    }

}