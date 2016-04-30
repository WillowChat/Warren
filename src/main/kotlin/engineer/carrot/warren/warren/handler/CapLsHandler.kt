package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.ircv3.CapEndMessage
import engineer.carrot.warren.kale.irc.message.ircv3.CapLsMessage
import engineer.carrot.warren.kale.irc.message.ircv3.CapReqMessage
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.state.CapLifecycle
import engineer.carrot.warren.warren.state.CapState

class CapLsHandler(val state: CapState, val messageSink: IMessageSink) : IKaleHandler<CapLsMessage> {
    override val messageType = CapLsMessage::class.java

    override fun handle(message: CapLsMessage) {
        val caps = message.caps
        val lifecycle = state.lifecycle

        state.server += message.caps

        println("server supports following caps: $caps")

        when(lifecycle) {
            CapLifecycle.NEGOTIATING -> {
                if (!message.isMultiline) {
                    val requestCaps = state.server.keys.intersect(state.negotiate)
                    val implicitlyRejectedCaps = state.negotiate.subtract(requestCaps)

                    state.rejected += implicitlyRejectedCaps

                    if (requestCaps.isEmpty()) {
                        println("server gave us caps and ended with a non-multiline ls, implicitly rejecting: $implicitlyRejectedCaps, nothing left so ending negotiation")

                        messageSink.write(CapEndMessage())
                    } else {
                        println("server gave us caps and ended with a non-multiline ls, requesting: $requestCaps, implicitly rejecting: $implicitlyRejectedCaps")

                        requestCaps.forEach { cap -> messageSink.write(CapReqMessage(caps = listOf(cap))) }
                    }
                } else {
                    println("server gave us a multiline cap ls, expecting more caps before ending")
                }
            }

            else -> println("server told us about caps but we don't think we're negotiating")
        }
    }
}

