package chat.willow.warren.extension.sasl

import chat.willow.kale.IKaleHandler
import chat.willow.kale.irc.message.extension.sasl.Rpl903Message
import chat.willow.kale.irc.tag.ITagStore
import chat.willow.warren.extension.cap.ICapManager
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.AuthLifecycle

class Rpl903Handler(val capManager: ICapManager, val saslState: SaslState) : IKaleHandler<Rpl903Message> {

    private val LOGGER = loggerFor<Rpl903Handler>()

    override val messageType = Rpl903Message::class.java

    override fun handle(message: Rpl903Message, tags: ITagStore) {
        LOGGER.debug("sasl auth successful for user: ${saslState.credentials?.account}")

        saslState.lifecycle = AuthLifecycle.AUTHED

        capManager.onRegistrationStateChanged()
    }

}

