package engineer.carrot.warren.warren.state

fun generateUsers(vararg nicks: String): MutableMap<String, ChannelUserState> {
    val users = mutableMapOf<String, ChannelUserState>()

    for (nick in nicks) {
        users += generateUser(nick)
    }

    return users
}

fun generateUser(nick: String): Pair<String, ChannelUserState> {
    return (nick to ChannelUserState(nick))
}