package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rfc1459.NoticeMessage
import engineer.carrot.warren.warren.loggerFor
import engineer.carrot.warren.warren.state.ChannelTypesState

class NoticeHandler(val channelTypesState: ChannelTypesState) : IKaleHandler<NoticeMessage> {
    private val LOGGER = loggerFor<NoticeHandler>()

    override val messageType = NoticeMessage::class.java

    override fun handle(message: NoticeMessage) {
        val source = message.source
        val target = message.target
        val messageContents = message.message

        if (source == null) {
            LOGGER.warn("got a Notice but the source was missing - bailing: $message")
            return
        }

        if (channelTypesState.types.any { char -> target.startsWith(char) }) {
            // Channel notice

            LOGGER.info("NOTICE: $target <${source.nick}> $messageContents")
        } else {
            // Private notice

            LOGGER.info("NOTICE PM: <${source.nick}> $messageContents")
        }
    }
}