package engineer.carrot.warren.warren

class WarrenConnection(val connectionInformation: WarrenConnectionInformation, val lineSourceSink: ILineSourceSink) {
    init {
        println("connection information: $connectionInformation")
    }

    fun connect() {
        val didSetUp = lineSourceSink.setUp()
        if (!didSetUp) {
            println("failed to set up line source/sink")

            return
        }

        lineSourceSink.writeLine("NICK ${connectionInformation.nickname}")
        lineSourceSink.writeLine("USER ${connectionInformation.nickname} 8 * ${connectionInformation.nickname}")

        var nextLine: String?
        do {
            nextLine = lineSourceSink.readLine() ?: break

            if (nextLine.startsWith("PING ")) {
                lineSourceSink.writeLine("PONG " + nextLine.substring(5))
            } else if (nextLine.endsWith("376 ${connectionInformation.nickname} :End of /MOTD command.")) {
                lineSourceSink.writeLine("JOIN #botdev")
            }
        } while (true)

        lineSourceSink.tearDown()
    }
}