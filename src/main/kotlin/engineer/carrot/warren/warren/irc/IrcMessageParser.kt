package engineer.carrot.warren.warren.irc

object IrcMessageParser: IIrcMessageParser {

    override fun parse(line: String): IrcMessage {
        throw UnsupportedOperationException()
    }

}