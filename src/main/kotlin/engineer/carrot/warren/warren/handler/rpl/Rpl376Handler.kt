package engineer.carrot.warren.warren.handler.rpl

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rfc1459.JoinMessage
import engineer.carrot.warren.kale.irc.message.rpl.Rpl376Message
import engineer.carrot.warren.kale.irc.message.utility.RawMessage
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.event.ConnectionLifecycleEvent
import engineer.carrot.warren.warren.event.IWarrenEventDispatcher
import engineer.carrot.warren.warren.loggerFor
import engineer.carrot.warren.warren.state.AuthLifecycle
import engineer.carrot.warren.warren.state.CapLifecycle
import engineer.carrot.warren.warren.state.ConnectionState
import engineer.carrot.warren.warren.state.LifecycleState

class Rpl376Handler(val eventDispatcher: IWarrenEventDispatcher, val sink: IMessageSink, val channelsToJoin: Map<String, String?>, val connectionState: ConnectionState) : IKaleHandler<Rpl376Message> {
    private val LOGGER = loggerFor<Rpl376Handler>()

    override val messageType = Rpl376Message::class.java

    override fun handle(message: Rpl376Message, tags: Map<String, String?>) {

        if (connectionState.cap.lifecycle == CapLifecycle.NEGOTIATING) {
            LOGGER.warn("got MOTD end before CAP end, assuming CAP negotiation failed")
            connectionState.cap.lifecycle = CapLifecycle.FAILED
        }

        when (connectionState.lifecycle) {
            LifecycleState.CONNECTING, LifecycleState.REGISTERING -> {
                LOGGER.debug("got end of MOTD")

                if (connectionState.nickServ.shouldAuth) {
                    val credentials = connectionState.nickServ.credentials
                    if (credentials == null) {
                        LOGGER.warn("asked to auth, but given no credentials, marking auth failed")

                        connectionState.nickServ.lifecycle = AuthLifecycle.AUTH_FAILED
                    } else {
                        LOGGER.debug("authing with nickserv - assuming success as replies aren't standardised (use SASL instead if you can)")

                        sink.writeRaw("NICKSERV identify ${credentials.account} ${credentials.password}")
                        connectionState.nickServ.lifecycle = AuthLifecycle.AUTHED

                        LOGGER.debug("waiting ${connectionState.nickServ.channelJoinWaitSeconds} seconds before joining channels")
                        try {
                            Thread.sleep(connectionState.nickServ.channelJoinWaitSeconds * 1000L)
                        } catch (exception: InterruptedException) {
                            LOGGER.warn("interrupted whilst waiting to join channels - bailing out")
                            return
                        }
                    }
                }

                LOGGER.debug("joining channels")
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

