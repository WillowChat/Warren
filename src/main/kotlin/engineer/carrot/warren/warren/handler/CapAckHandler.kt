package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.ircv3.CapEndMessage
import engineer.carrot.warren.kale.irc.message.ircv3.CapAckMessage
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.state.CapLifecycle
import engineer.carrot.warren.warren.state.CapState
import engineer.carrot.warren.warren.state.ConnectionState

class CapAckHandler(val state: CapState, val sink: IMessageSink) : IKaleHandler<CapAckMessage> {
    override val messageType = CapAckMessage::class.java

    override fun handle(message: CapAckMessage) {
        val caps = message.caps
        val lifecycle = state.lifecycle

        println("server ACKed following caps: $caps")

        state.accepted += caps

        when(lifecycle) {
            CapLifecycle.NEGOTIATING -> {
                println("server ACKed some caps, checked if it's the last reply")

                val remainingCaps = state.negotiate.subtract(state.accepted).subtract(state.rejected)
                println("remaining caps to negotiate: $remainingCaps")

                if (remainingCaps.isEmpty()) {
                    state.lifecycle = CapLifecycle.NEGOTIATED

                    println("no more remaining caps, ending cap negotiation with state: $state")

                    sink.write(CapEndMessage())
                }
            }

            else -> println("server ACKed caps but we don't think we're negotiating")
        }
    }
}

