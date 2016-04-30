package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.ircv3.CapEndMessage
import engineer.carrot.warren.kale.irc.message.ircv3.CapNakMessage
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.state.CapLifecycle
import engineer.carrot.warren.warren.state.CapState
import engineer.carrot.warren.warren.state.ConnectionState

class CapNakHandler(val state: CapState, val messageSink: IMessageSink) : IKaleHandler<CapNakMessage> {
    override val messageType = CapNakMessage::class.java

    override fun handle(message: CapNakMessage) {
        val caps = message.caps
        val lifecycle = state.lifecycle

        println("server NAKed following caps: $caps")

        state.rejected += caps

        when(lifecycle) {
            CapLifecycle.NEGOTIATING -> {
                println("server NAKed some caps, checked if it's the last reply")

                val remainingCaps = state.negotiate.subtract(state.accepted).subtract(state.rejected)
                println("remaining caps to negotiate: $remainingCaps")

                if (remainingCaps.isEmpty()) {
                    state.lifecycle = CapLifecycle.NEGOTIATED

                    println("no more remaining caps, ending cap negotiation with state: $state")

                    messageSink.write(CapEndMessage())
                }
            }

            else -> println("server NAKed caps but we don't think we're negotiating")
        }
    }
}

