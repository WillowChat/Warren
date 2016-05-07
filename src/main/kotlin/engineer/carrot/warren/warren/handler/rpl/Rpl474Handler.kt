package engineer.carrot.warren.warren.handler.rpl

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rfc1459.JoinMessage
import engineer.carrot.warren.kale.irc.message.rpl.Rpl474Message
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.state.ChannelsState
import engineer.carrot.warren.warren.state.ConnectionState
import engineer.carrot.warren.warren.state.JoiningChannelLifecycle
import engineer.carrot.warren.warren.state.LifecycleState

class Rpl474Handler(val channelsState: ChannelsState) : IKaleHandler<Rpl474Message> {
    override val messageType = Rpl474Message::class.java

    override fun handle(message: Rpl474Message) {
        val channel = channelsState.joining[message.channel]

        if (channel == null) {
            println("got a banned from channel reply for a channel we don't think we're joining: $message")
            println("channels state: $channelsState")
            return
        }

        println("we are banned from channel, failed to join: $channel")
        channel.status = JoiningChannelLifecycle.FAILED

        println("new channels state: $channelsState")
    }
}

