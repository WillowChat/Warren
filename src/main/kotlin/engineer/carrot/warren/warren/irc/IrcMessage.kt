package engineer.carrot.warren.warren.irc

data class IrcMessage(val tags: Map<String, String>, val prefix: String?, val command: String, val parameters: List<String>)