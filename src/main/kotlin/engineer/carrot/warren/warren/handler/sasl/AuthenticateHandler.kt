package engineer.carrot.warren.warren.handler.sasl

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.ircv3.sasl.AuthenticateMessage
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.loggerFor
import engineer.carrot.warren.warren.state.AuthLifecycle
import engineer.carrot.warren.warren.state.SaslState
import java.util.*

class AuthenticateHandler(val state: SaslState, val sink: IMessageSink) : IKaleHandler<AuthenticateMessage> {
    private val LOGGER = loggerFor<AuthenticateHandler>()

    override val messageType = AuthenticateMessage::class.java

    override fun handle(message: AuthenticateMessage, tags: Map<String, String?>) {
        if (state.lifecycle != AuthLifecycle.AUTHING) {
            LOGGER.warn("got an auth challenge, but we don't think we're authenticating - ignoring: $message")
            return
        }

        val credentials = state.credentials
        if (credentials == null) {
            LOGGER.warn("wanted to do SASL auth, but don't have credentials set - bailing: $state")
            return
        }

        val saslBytes = ("${credentials.account}\u0000${credentials.account}\u0000${credentials.password}").toByteArray()
        val saslString = Base64.getEncoder().encode(saslBytes).toString(Charsets.UTF_8)

        LOGGER.debug("replied to sasl auth request for ${credentials.account}")
        sink.write(AuthenticateMessage(payload = saslString, isEmpty = false))
    }
}