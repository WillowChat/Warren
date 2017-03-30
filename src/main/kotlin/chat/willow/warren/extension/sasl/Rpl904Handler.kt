package chat.willow.warren.extension.sasl

import chat.willow.kale.IMetadataStore
import chat.willow.kale.KaleHandler
import chat.willow.kale.irc.message.extension.sasl.rpl.Rpl904Message
import chat.willow.kale.irc.message.extension.sasl.rpl.Rpl904MessageType
import chat.willow.warren.extension.cap.ICapManager
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.AuthLifecycle

class Rpl904Handler(val capManager: ICapManager, val saslState: SaslState) : KaleHandler<Rpl904MessageType>(Rpl904Message.Parser) {

    private val LOGGER = loggerFor<Rpl904Handler>()


    override fun handle(message: Rpl904MessageType, metadata: IMetadataStore) {
        LOGGER.warn("invalid mechanism, or sasl auth failed: ${message.contents}")

        saslState.lifecycle = AuthLifecycle.AUTH_FAILED

        capManager.onRegistrationStateChanged()
    }

}

