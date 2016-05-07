package engineer.carrot.warren.warren.handler.rpl

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rfc1459.JoinMessage
import engineer.carrot.warren.kale.irc.message.rpl.Rpl473Message
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.state.ChannelsState
import engineer.carrot.warren.warren.state.ConnectionState
import engineer.carrot.warren.warren.state.JoiningChannelLifecycle
import engineer.carrot.warren.warren.state.LifecycleState

class Rpl473Handler(val channelsState: ChannelsState) : IKaleHandler<Rpl473Message> {
    override val messageType = Rpl473Message::class.java

    override fun handle(message: Rpl473Message) {
        val channel = channelsState.joining[message.channel]

        if (channel == null) {
            println("got an invite only channel reply for a channel we don't think we're joining: $message")
            println("channels state: $channelsState")
            return
        }

        println("channel is invite only, failed to join: $channel")
        channel.status = JoiningChannelLifecycle.FAILED

        println("new channels state: $channelsState")
    }
}

