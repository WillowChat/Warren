package engineer.carrot.warren.warren

import engineer.carrot.warren.warren.irc.IMessageSink
import engineer.carrot.warren.warren.irc.IMessageSource
import engineer.carrot.warren.warren.irc.message.IrcMessage

class IrcRunner(val connectionInfo: ConnectionInfo, val messageSource: IMessageSource, val messageSink: IMessageSink) {

    fun run() {
        messageSink.writeMessage(IrcMessage(command = "NICK", parameters = listOf(connectionInfo.nickname)))
        messageSink.writeMessage(IrcMessage(command = "USER", parameters = listOf(connectionInfo.nickname, "8", "*", connectionInfo.nickname)))

        do {
            val (line, message) = messageSource.nextMessage() ?: break

            if (message == null) {
                println("failed to parse line, ignoring: $line")

                continue
            }

            println("processing: $message")

            if (message.command == "PING") {
                messageSink.writeMessage(IrcMessage(command = "PONG", parameters = listOf(message.parameters.getOrNull(0) ?: "TOKEN")))
            } else if (message.command == "376" && message.parameters.getOrNull(0) == connectionInfo.nickname) {
                messageSink.writeMessage(IrcMessage(command = "JOIN", parameters = listOf("#botdev")))
            }
        } while (true)
    }
}