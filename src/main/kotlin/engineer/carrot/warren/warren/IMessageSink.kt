package engineer.carrot.warren.warren

import engineer.carrot.warren.kale.irc.message.IMessage

interface IMessageSink {
    fun <T: IMessage> writeMessage(message: T)
}