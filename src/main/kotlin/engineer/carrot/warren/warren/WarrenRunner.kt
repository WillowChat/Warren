package engineer.carrot.warren.warren

object WarrenRunner {
    @JvmStatic fun main(args: Array<String>) {
        val connectionInformation = WarrenConnectionInformation(server = args[0], port = args[1].toInt(), nickname = args[2])

        val socketSourceSink = SocketLineSourceSink(connectionInformation.server, connectionInformation.port)
        val connection = WarrenConnection(connectionInformation, socketSourceSink)

        connection.connect()
    }
}