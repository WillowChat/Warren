package chat.willow.warren.handler.rpl

import chat.willow.kale.IKaleHandler
import chat.willow.kale.irc.message.rfc1459.rpl.Rpl376Message
import chat.willow.warren.IMessageSink
import chat.willow.warren.extension.cap.CapLifecycle
import chat.willow.warren.extension.cap.CapState
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.registration.IRegistrationExtension

class Rpl376Handler(val sink: IMessageSink, val capState: CapState, val rfc1459RegistrationExtension: IRegistrationExtension, val capRegistrationExtension: IRegistrationExtension) : IKaleHandler<Rpl376Message> {

    private val LOGGER = loggerFor<Rpl376Handler>()

    override val messageType = Rpl376Message::class.java

    override fun handle(message: Rpl376Message, tags: Map<String, String?>) {

        if (capState.lifecycle == CapLifecycle.NEGOTIATING) {
            LOGGER.warn("got MOTD end before CAP end, assuming CAP negotiation failed")
            capState.lifecycle = CapLifecycle.FAILED

            capRegistrationExtension.onRegistrationFailed()
        }

        rfc1459RegistrationExtension.onRegistrationSucceeded()
    }

}

