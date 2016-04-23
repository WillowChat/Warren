package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rfc1459.NoticeMessage
import engineer.carrot.warren.kale.irc.message.rfc1459.PongMessage
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.state.ChannelTypesState

class NoticeHandler(val channelTypesState: ChannelTypesState) : IKaleHandler<NoticeMessage> {
    override val messageType = NoticeMessage::class.java

    override fun handle(message: NoticeMessage) {
        val source = message.source
        val target = message.target
        val messageContents = message.message

        if (source == null) {
            println("got a Notice but the source was missing - bailing: $message")
            return
        }

        if (channelTypesState.types.any { char -> target.startsWith(char) }) {
            // Channel notice

            println("NOTICE: $target <${source.nick}> $messageContents")
        } else {
            // Private notice

            println("NOTICE PM: <${source.nick}> $messageContents")
        }
    }
}