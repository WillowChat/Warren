package chat.willow.warren.extension.sasl

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.extension.sasl.Rpl904Message
import chat.willow.warren.extension.cap.ICapManager
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.AuthLifecycle

class Rpl904Handler(val capManager: ICapManager, val saslState: SaslState) : IKaleHandler<Rpl904Message> {

    private val LOGGER = loggerFor<Rpl904Handler>()

    override val messageType = Rpl904Message::class.java

    override fun handle(message: Rpl904Message, tags: Map<String, String?>) {
        LOGGER.warn("invalid mechanism, or sasl auth failed: ${message.contents}")

        saslState.lifecycle = AuthLifecycle.AUTH_FAILED

        capManager.onRegistrationStateChanged()
    }

}

