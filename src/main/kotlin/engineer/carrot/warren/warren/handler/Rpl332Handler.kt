package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rfc1459.JoinMessage
import engineer.carrot.warren.kale.irc.message.rpl.Rpl332Message
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.state.ChannelsState
import engineer.carrot.warren.warren.state.ConnectionState
import engineer.carrot.warren.warren.state.LifecycleState

class Rpl332Handler(val channelsState: ChannelsState) : IKaleHandler<Rpl332Message> {
    override val messageType = Rpl332Message::class.java

    override fun handle(message: Rpl332Message) {
        val channel = channelsState.joined[message.channel]
        val topic = message.topic

        if (channel == null) {
            println("got a topic for a channel we don't think we're in, not doing anything: $message")
            return
        }

        println("channel topic for ${channel.name}: $topic")
        channel.topic = topic
    }
}

