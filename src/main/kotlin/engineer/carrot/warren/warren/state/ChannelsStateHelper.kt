package engineer.carrot.warren.warren.state

fun generateUsers(vararg nicks: String): MutableMap<String, ChannelUserState> {
    val users = mutableMapOf<String, ChannelUserState>()

    for (nick in nicks) {
        users += generateUser(nick)
    }

    return users
}

fun generateUsersWithModes(vararg nicks: Pair<String, Set<Char>>): MutableMap<String, ChannelUserState> {
    val users = mutableMapOf<String, ChannelUserState>()

    for ((nick, modes) in nicks) {
        users += generateUser(nick, modes)
    }

    return users
}


fun generateUser(nick: String): Pair<String, ChannelUserState> {
    return (nick to ChannelUserState(nick))
}

fun generateUser(nick: String, modes: Set<Char>): Pair<String, ChannelUserState> {
    return (nick to ChannelUserState(nick, modes.toMutableSet()))
}