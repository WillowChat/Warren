package chat.willow.warren.registration

import chat.willow.kale.irc.message.rfc1459.NickMessage
import chat.willow.kale.irc.message.rfc1459.PassMessage
import chat.willow.kale.irc.message.rfc1459.UserMessage
import chat.willow.warren.IMessageSink

class RFC1459RegistrationExtension(private val sink: IMessageSink, private val nickname: String, private val username: String, private val password: String? = null, private val registrationManager: IRegistrationManager): IRegistrationExtension {

    override fun startRegistration() {
        if (password != null) {
            sink.write(PassMessage.Command(password = password))
        }

        sink.write(NickMessage.Command(nickname = nickname))
        sink.write(UserMessage.Command(username = username, mode = "8", realname = username))
    }

    override fun onRegistrationSucceeded() {
        registrationManager.onExtensionSuccess(this)
    }

    override fun onRegistrationFailed() {
        registrationManager.onExtensionFailure(this)
    }

}
