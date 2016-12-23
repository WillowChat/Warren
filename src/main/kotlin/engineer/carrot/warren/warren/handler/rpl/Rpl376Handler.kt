package engineer.carrot.warren.warren.handler.rpl

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rfc1459.rpl.Rpl376Message
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.extension.cap.CapLifecycle
import engineer.carrot.warren.warren.extension.cap.CapState
import engineer.carrot.warren.warren.helper.loggerFor
import engineer.carrot.warren.warren.registration.IRegistrationExtension

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

