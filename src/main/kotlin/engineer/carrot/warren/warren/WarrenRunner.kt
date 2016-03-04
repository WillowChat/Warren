package engineer.carrot.warren.warren

import engineer.carrot.warren.warren.irc.message.IrcMessageParser
import engineer.carrot.warren.warren.irc.message.IrcMessageSerialiser

object WarrenRunner {
    @JvmStatic fun main(args: Array<String>) {
        val connectionInformation = ConnectionInfo(server = args[0], port = args[1].toInt(), nickname = args[2])

        val parser = IrcMessageParser
        val serialiser = IrcMessageSerialiser

        val socket = IrcSocket(connectionInformation.server, connectionInformation.port, parser, serialiser)

        if (!socket.setUp()) {
            println("failed to set up irc socket for: ${connectionInformation}")
            return
        }

        val connection = IrcRunner(connectionInformation, messageSource = socket, messageSink = socket)
        connection.run()

        socket.tearDown()
    }
}