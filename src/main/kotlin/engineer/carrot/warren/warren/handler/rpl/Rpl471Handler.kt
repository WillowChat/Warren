package engineer.carrot.warren.warren.handler.rpl

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rfc1459.JoinMessage
import engineer.carrot.warren.kale.irc.message.rpl.Rpl471Message
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.state.ChannelsState
import engineer.carrot.warren.warren.state.ConnectionState
import engineer.carrot.warren.warren.state.JoiningChannelLifecycle
import engineer.carrot.warren.warren.state.LifecycleState

class Rpl471Handler(val channelsState: ChannelsState) : IKaleHandler<Rpl471Message> {
    override val messageType = Rpl471Message::class.java

    override fun handle(message: Rpl471Message) {
        val channel = channelsState.joining[message.channel]

        if (channel == null) {
            println("got a full channel reply for a channel we don't think we're joining: $message")
            println("channels state: $channelsState")
            return
        }

        println("channel is full, failed to join: $channel")
        channel.status = JoiningChannelLifecycle.FAILED

        println("new channels state: $channelsState")
    }
}

