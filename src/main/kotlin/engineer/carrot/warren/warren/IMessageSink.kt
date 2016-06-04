package engineer.carrot.warren.warren

interface IMessageSink {
    fun write(message: Any)

    fun setUp(): Boolean

    fun tearDown()
}