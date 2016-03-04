package engineer.carrot.warren.warren.irc

import engineer.carrot.warren.warren.irc.message.IrcMessage

interface IMessageSource {
    fun nextMessage(): Pair<String, IrcMessage?>?
}