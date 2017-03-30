package chat.willow.warren.extension.sasl

import chat.willow.kale.IMetadataStore
import chat.willow.kale.KaleHandler
import chat.willow.kale.irc.message.extension.sasl.rpl.Rpl905Message
import chat.willow.kale.irc.message.extension.sasl.rpl.Rpl905MessageType
import chat.willow.warren.extension.cap.ICapManager
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.AuthLifecycle

class Rpl905Handler(val capManager: ICapManager, val saslState: SaslState) : KaleHandler<Rpl905MessageType>(Rpl905Message.Parser) {

    private val LOGGER = loggerFor<Rpl905Handler>()


    override fun handle(message: Rpl905MessageType, metadata: IMetadataStore) {
        LOGGER.warn("sasl auth failed: ${message.contents}")

        saslState.lifecycle = AuthLifecycle.AUTH_FAILED

        capManager.onRegistrationStateChanged()
    }

}

