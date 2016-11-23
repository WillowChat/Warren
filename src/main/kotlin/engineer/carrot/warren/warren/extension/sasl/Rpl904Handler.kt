package engineer.carrot.warren.warren.extension.sasl

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.extension.sasl.Rpl904Message
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.extension.cap.ICapManager
import engineer.carrot.warren.warren.loggerFor
import engineer.carrot.warren.warren.state.AuthLifecycle

class Rpl904Handler(val capManager: ICapManager, val saslState: SaslState, val sink: IMessageSink) : IKaleHandler<Rpl904Message> {

    private val LOGGER = loggerFor<Rpl904Handler>()

    override val messageType = Rpl904Message::class.java

    override fun handle(message: Rpl904Message, tags: Map<String, String?>) {
        LOGGER.warn("invalid mechanism, or sasl auth failed: ${message.contents}")

        saslState.lifecycle = AuthLifecycle.AUTH_FAILED

        capManager.onRegistrationStateChanged()
    }

}

