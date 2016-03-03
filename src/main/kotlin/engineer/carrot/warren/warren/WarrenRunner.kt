package engineer.carrot.warren.warren

object WarrenRunner {
    @JvmStatic fun main(args: Array<String>) {
        val connectionInformation = ConnectionInfo(server = args[0], port = args[1].toInt(), nickname = args[2])

        val socket = IRCSocket(connectionInformation.server, connectionInformation.port)

        if (!socket.setUp()) {
            println("failed to set up irc socket for: ${connectionInformation}")
            return
        }

        val connection = IRCConnection(connectionInformation, lineSource = socket, lineSink = socket)
        connection.run()

        socket.tearDown()
    }
}