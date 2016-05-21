package engineer.carrot.warren.warren.handler.rpl

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rfc1459.JoinMessage
import engineer.carrot.warren.kale.irc.message.rpl.Rpl376Message
import engineer.carrot.warren.warren.ConnectionLifecycleEvent
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.IWarrenEventDispatcher
import engineer.carrot.warren.warren.loggerFor
import engineer.carrot.warren.warren.state.CapLifecycle
import engineer.carrot.warren.warren.state.ConnectionState
import engineer.carrot.warren.warren.state.LifecycleState

class Rpl376Handler(val eventDispatcher: IWarrenEventDispatcher, val sink: IMessageSink, val channelsToJoin: Map<String, String?>, val connectionState: ConnectionState) : IKaleHandler<Rpl376Message> {
    private val LOGGER = loggerFor<Rpl376Handler>()

    override val messageType = Rpl376Message::class.java

    override fun handle(message: Rpl376Message) {

        if (connectionState.cap.lifecycle == CapLifecycle.NEGOTIATING) {
            LOGGER.warn("got MOTD end before CAP end, assuming CAP negotiation failed")
            connectionState.cap.lifecycle = CapLifecycle.FAILED
        }

        when (connectionState.lifecycle) {
            LifecycleState.CONNECTING, LifecycleState.REGISTERING -> {
                LOGGER.debug("got end of MOTD, updating lifecycle to CONNECTED and joining channels")
                join(channelsToJoin, sink)
            }

            else -> LOGGER.warn("got end of MOTD but we don't think we're connecting")
        }

        connectionState.lifecycle = LifecycleState.CONNECTED
        eventDispatcher.fire(ConnectionLifecycleEvent(LifecycleState.CONNECTED))
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

