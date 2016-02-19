package engineer.carrot.warren.warren

interface ILineSourceSink {
    fun setUp(): Boolean

    fun tearDown()

    fun readLine(): String?

    fun writeLine(line: String)
}