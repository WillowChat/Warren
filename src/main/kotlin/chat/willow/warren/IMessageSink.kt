package chat.willow.warren

interface IMessageSink {

    fun write(message: Any)
    fun writeRaw(line: String)
    fun setUp(): Boolean
    fun tearDown()

}