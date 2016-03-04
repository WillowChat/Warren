package engineer.carrot.warren.warren.irc.message

interface IIrcMessageSerialiser {
    fun serialise(message: IrcMessage): String?
}

