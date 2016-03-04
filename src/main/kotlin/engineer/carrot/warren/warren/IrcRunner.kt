package engineer.carrot.warren.warren

import engineer.carrot.warren.warren.irc.message.IrcMessageParser

class IrcRunner(val connectionInfo: ConnectionInfo, val lineSource: ILineSource, val lineSink: ILineSink) {

    fun run() {
        lineSink.writeLine("NICK ${connectionInfo.nickname}")
        lineSink.writeLine("USER ${connectionInfo.nickname} 8 * ${connectionInfo.nickname}")

        var nextLine: String?
        do {
            nextLine = lineSource.readLine() ?: break

            val message = IrcMessageParser.parse(nextLine)
            if (message == null) {
                println("failed to parse line")

                continue
            } else {
                println("parsed to: ${message}")
            }

            if (message.command == "PING") {
                lineSink.writeLine("PONG :" + (message.parameters.getOrNull(0) ?: "MYTOKEN"))
            } else if (nextLine.endsWith("376 ${connectionInfo.nickname} :End of /MOTD command.")) {
                lineSink.writeLine("JOIN #botdev")
            }
        } while (true)
    }
}