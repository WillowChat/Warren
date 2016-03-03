package engineer.carrot.warren.warren

class IrcRunner(val connectionInfo: ConnectionInfo, val lineSource: ILineSource, val lineSink: ILineSink) {

    fun run() {
        lineSink.writeLine("NICK ${connectionInfo.nickname}")
        lineSink.writeLine("USER ${connectionInfo.nickname} 8 * ${connectionInfo.nickname}")

        var nextLine: String?
        do {
            nextLine = lineSource.readLine() ?: break

            if (nextLine.startsWith("PING ")) {
                lineSink.writeLine("PONG " + nextLine.substring(5))
            } else if (nextLine.endsWith("376 ${connectionInfo.nickname} :End of /MOTD command.")) {
                lineSink.writeLine("JOIN #botdev")
            }
        } while (true)
    }
}