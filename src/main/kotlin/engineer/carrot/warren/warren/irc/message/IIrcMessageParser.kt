package engineer.carrot.warren.warren.irc.message

interface IIrcMessageParser {
    fun parse(line: String): IrcMessage?
}