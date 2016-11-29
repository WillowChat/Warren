package engineer.carrot.warren.warren.registration

import engineer.carrot.warren.kale.irc.message.rfc1459.NickMessage
import engineer.carrot.warren.kale.irc.message.rfc1459.PassMessage
import engineer.carrot.warren.kale.irc.message.rfc1459.UserMessage
import engineer.carrot.warren.warren.IMessageSink

class RFC1459RegistrationExtension(private val sink: IMessageSink, private val nickname: String, private val username: String, private val password: String? = null, private val registrationManager: IRegistrationManager): IRegistrationExtension {

    override fun startRegistration() {
        if (password != null) {
            sink.write(PassMessage(password = password))
        }

        sink.write(NickMessage(nickname = nickname))
        sink.write(UserMessage(username = username, mode = "8", realname = username))
    }

    override fun onRegistrationSucceeded() {
        registrationManager.onExtensionSuccess(this)
    }

    override fun onRegistrationFailed() {
        registrationManager.onExtensionFailure(this)
    }

}
