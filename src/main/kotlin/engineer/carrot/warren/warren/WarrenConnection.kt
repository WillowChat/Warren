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
            nextLine = lineSourceSink.readLine()

            if (nextLine == null) {
                break
            }

            println(nextLine)
        } while (true)

        lineSourceSink.tearDown()
    }
}