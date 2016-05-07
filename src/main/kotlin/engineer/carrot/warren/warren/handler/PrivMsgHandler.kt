package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rfc1459.PrivMsgMessage
import engineer.carrot.warren.kale.irc.message.rfc1459.PongMessage
import engineer.carrot.warren.warren.ChannelMessageEvent
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.IWarrenEventDispatcher
import engineer.carrot.warren.warren.PrivateMessageEvent
import engineer.carrot.warren.warren.state.ChannelTypesState

class PrivMsgHandler(val eventDispatcher: IWarrenEventDispatcher, val channelTypesState: ChannelTypesState) : IKaleHandler<PrivMsgMessage> {
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

            eventDispatcher.fire(ChannelMessageEvent(user = source, channel = target, message = messageContents))

            println("$serverTime$target <${source.nick}> $messageContents")
        } else {
            // Private message

            println("PM: $serverTime<${source.nick}> $messageContents")

            eventDispatcher.fire(PrivateMessageEvent(user = source, message = messageContents))
        }
    }
}