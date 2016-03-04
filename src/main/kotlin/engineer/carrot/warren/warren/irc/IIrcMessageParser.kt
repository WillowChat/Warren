package engineer.carrot.warren.warren.irc

interface IIrcMessageParser {
    fun parse(line: String): IrcMessage?
}