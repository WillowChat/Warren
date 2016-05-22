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

fun emptyChannelsState(mappingState: CaseMappingState): ChannelsState {
    return ChannelsState(joining = JoiningChannelsState(mappingState), joined = JoinedChannelsState(mappingState))
}

fun channelsStateWith(joined: Collection<ChannelState>, mappingState: CaseMappingState): ChannelsState {
    val joinedChannels = JoinedChannelsState(mappingState)

    joinedChannels += joined

    return ChannelsState(joining = JoiningChannelsState(mappingState), joined = joinedChannels)
}

fun joiningChannelsStateWith(joining: Collection<JoiningChannelState>, mappingState: CaseMappingState): ChannelsState {
    val joiningChannels = JoiningChannelsState(mappingState)

    joiningChannels += joining

    return ChannelsState(joining = joiningChannels, joined = JoinedChannelsState(mappingState))
}