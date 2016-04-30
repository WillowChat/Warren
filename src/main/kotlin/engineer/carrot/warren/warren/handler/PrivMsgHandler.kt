package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rfc1459.PrivMsgMessage
import engineer.carrot.warren.kale.irc.message.rfc1459.PongMessage
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.state.ChannelTypesState

class PrivMsgHandler(val channelTypesState: ChannelTypesState) : IKaleHandler<PrivMsgMessage> {
    override val messageType = PrivMsgMessage::class.java

    override fun handle(message: PrivMsgMessage) {
        val source = message.source
        val target = message.target
        val messageContents = message.message

        if (source == null) {
            println("got a PrivMsg but the source was missing - bailing: $message")
            return
        }

        var serverTime = ""
        if (message.time != null) {
            serverTime = "${message.time} "
        }

        if (channelTypesState.types.any { char -> target.startsWith(char) }) {
            // Channel message

            println("$serverTime$target <${source.nick}> $messageContents")
        } else {
            // Private message

            println("PM: $serverTime<${source.nick}> $messageContents")
        }
    }
}