package engineer.carrot.warren.warren

import engineer.carrot.warren.kale.irc.message.IMessage

interface IMessageSink {
    fun <T : IMessage> write(message: T)

    fun setUp(): Boolean
}