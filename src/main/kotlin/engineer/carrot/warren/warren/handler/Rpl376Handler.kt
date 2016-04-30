package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rfc1459.JoinMessage
import engineer.carrot.warren.kale.irc.message.rpl.Rpl376Message
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.state.CapLifecycle
import engineer.carrot.warren.warren.state.ConnectionState
import engineer.carrot.warren.warren.state.LifecycleState

class Rpl376Handler(val sink: IMessageSink, val channelsToJoin: Map<String, String?>, val connectionState: ConnectionState) : IKaleHandler<Rpl376Message> {
    override val messageType = Rpl376Message::class.java

    override fun handle(message: Rpl376Message) {

        if (connectionState.cap.lifecycle == CapLifecycle.NEGOTIATING) {
            println("got MOTD end before CAP end, assuming CAP negotiation failed")
            connectionState.cap.lifecycle = CapLifecycle.FAILED
        }

        when(connectionState.lifecycle) {
            LifecycleState.CONNECTING, LifecycleState.REGISTERING -> {
                println("got end of MOTD, updating lifecycle to CONNECTED and joining channels")
                join(channelsToJoin, sink)
            }

            else -> println("got end of MOTD but we don't think we're connecting")
        }

        connectionState.lifecycle = LifecycleState.CONNECTED
    }

    private fun join(channelsWithKeys: Map<String, String?>, sink: IMessageSink) {
        for ((channel, key) in channelsWithKeys) {
            if (key != null) {
                sink.write(JoinMessage(channels = listOf(channel), keys = listOf(key)))
            } else {
                sink.write(JoinMessage(channels = listOf(channel)))
            }
        }
    }
}

