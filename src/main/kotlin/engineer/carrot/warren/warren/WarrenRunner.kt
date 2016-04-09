package engineer.carrot.warren.warren

import engineer.carrot.warren.kale.Kale
import engineer.carrot.warren.kale.irc.message.IrcMessageSerialiser

object WarrenRunner {
    @JvmStatic fun main(args: Array<String>) {
        val connectionInformation = ConnectionInfo(server = args[0], port = args[1].toInt(), nickname = args[2])

        val kale = Kale().addDefaultMessages()
        val serialiser = IrcMessageSerialiser

        val socket = IrcSocket(connectionInformation.server, connectionInformation.port, kale, serialiser)

        if (!socket.setUp()) {
            println("failed to set up irc socket for: ${connectionInformation}")
            return
        }

        val connection = IrcRunner(connectionInformation, kale = kale, sink = socket, processor = socket)
        connection.run()

        socket.tearDown()
    }
}