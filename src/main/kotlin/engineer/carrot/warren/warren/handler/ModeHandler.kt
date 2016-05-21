package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rfc1459.ModeMessage
import engineer.carrot.warren.warren.ChannelModeEvent
import engineer.carrot.warren.warren.IWarrenEventDispatcher
import engineer.carrot.warren.warren.UserModeEvent
import engineer.carrot.warren.warren.loggerFor
import engineer.carrot.warren.warren.state.ChannelTypesState

class ModeHandler(val eventDispatcher: IWarrenEventDispatcher, val channelTypesState: ChannelTypesState) : IKaleHandler<ModeMessage> {
    private val LOGGER = loggerFor<ModeHandler>()

    override val messageType = ModeMessage::class.java

    override fun handle(message: ModeMessage) {
        val target = message.target

        if (channelTypesState.types.any { char -> target.startsWith(char) }) {
            // Channel mode

            for (modifier in message.modifiers) {
                eventDispatcher.fire(ChannelModeEvent(user = message.source, channel = target, modifier = modifier))
            }
        } else {
            // User mode

            LOGGER.info("user changed modes: $message")

            for (modifier in message.modifiers) {
                eventDispatcher.fire(UserModeEvent(user = target, modifier = modifier))
            }
        }
    }
}