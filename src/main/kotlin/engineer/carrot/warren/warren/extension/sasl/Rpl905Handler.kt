package engineer.carrot.warren.warren.extension.sasl

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.extension.sasl.Rpl905Message
import engineer.carrot.warren.warren.extension.cap.ICapManager
import engineer.carrot.warren.warren.helper.loggerFor
import engineer.carrot.warren.warren.state.AuthLifecycle

class Rpl905Handler(val capManager: ICapManager, val saslState: SaslState) : IKaleHandler<Rpl905Message> {

    private val LOGGER = loggerFor<Rpl905Handler>()

    override val messageType = Rpl905Message::class.java

    override fun handle(message: Rpl905Message, tags: Map<String, String?>) {
        LOGGER.warn("sasl auth failed: ${message.contents}")

        saslState.lifecycle = AuthLifecycle.AUTH_FAILED

        capManager.onRegistrationStateChanged()
    }

}

