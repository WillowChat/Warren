package engineer.carrot.warren.warren.extension.sasl

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.extension.sasl.Rpl903Message
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.extension.cap.ICapManager
import engineer.carrot.warren.warren.loggerFor
import engineer.carrot.warren.warren.state.AuthLifecycle

class Rpl903Handler(val capManager: ICapManager, val saslState: SaslState, val sink: IMessageSink) : IKaleHandler<Rpl903Message> {

    private val LOGGER = loggerFor<Rpl903Handler>()

    override val messageType = Rpl903Message::class.java

    override fun handle(message: Rpl903Message, tags: Map<String, String?>) {
        LOGGER.debug("sasl auth successful for user: ${saslState.credentials?.account}")

        saslState.lifecycle = AuthLifecycle.AUTHED

        capManager.onRegistrationStateChanged()
    }

}

