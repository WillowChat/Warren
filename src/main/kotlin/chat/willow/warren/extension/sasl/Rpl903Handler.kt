package chat.willow.warren.extension.sasl

import chat.willow.kale.IMetadataStore
import chat.willow.kale.KaleHandler
import chat.willow.kale.irc.message.extension.sasl.rpl.Rpl903Message
import chat.willow.kale.irc.message.extension.sasl.rpl.Rpl903MessageType
import chat.willow.warren.extension.cap.ICapManager
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.AuthLifecycle

class Rpl903Handler(val capManager: ICapManager, val saslState: SaslState) : KaleHandler<Rpl903MessageType>(Rpl903Message.Parser) {

    private val LOGGER = loggerFor<Rpl903Handler>()


    override fun handle(message: Rpl903MessageType, metadata: IMetadataStore) {
        LOGGER.debug("sasl auth successful for user: ${saslState.credentials?.account}")

        saslState.lifecycle = AuthLifecycle.AUTHED

        capManager.onRegistrationStateChanged()
    }

}

