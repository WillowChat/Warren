package engineer.carrot.warren.warren.irc

import engineer.carrot.warren.warren.irc.message.IrcMessage

interface IMessageSink {
    fun writeMessage(message: IrcMessage)
}