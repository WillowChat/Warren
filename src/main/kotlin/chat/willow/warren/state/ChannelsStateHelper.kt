package chat.willow.warren.state

import chat.willow.kale.irc.message.utility.CaseMapping
import chat.willow.kale.irc.prefix.Prefix

fun generateUsersFromNicks(nicks: List<String>, mappingState: CaseMappingState = CaseMappingState(CaseMapping.RFC1459)): ChannelUsersState {
    return generateUsersFromPrefixes(prefixes = nicks.map { Prefix(nick = it) })
}

fun generateUsersFromPrefixes(prefixes: List<Prefix>, mappingState: CaseMappingState = CaseMappingState(CaseMapping.RFC1459)): ChannelUsersState {
    val users = ChannelUsersState(mappingState)

    for (prefix in prefixes) {
        users += ChannelUserState(prefix)
    }

    return users
}

fun generateUsersWithModes(vararg nicks: Pair<String, Set<Char>>, mappingState: CaseMappingState): ChannelUsersState {
    val users = ChannelUsersState(mappingState)

    for ((nick, modes) in nicks) {
        users += generateUser(nick, modes)
    }

    return users
}

fun generateUser(nick: String, modes: Set<Char>): ChannelUserState {
    return ChannelUserState(Prefix(nick = nick), modes = modes.toMutableSet())
}

fun generateUser(nick: String, account: String? = null, awayMessage: String? = null): ChannelUserState {
    return ChannelUserState(Prefix(nick = nick), account = account, awayMessage = awayMessage)
}

fun generateChannelUsersState(vararg users: ChannelUserState, mappingState: CaseMappingState = CaseMappingState(CaseMapping.RFC1459)): ChannelUsersState {
    val channelUsers = ChannelUsersState(mappingState)

    for (user in users) {
        channelUsers += user
    }

    return channelUsers
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

fun emptyChannel(name: String, mappingState: CaseMappingState = CaseMappingState(mapping = CaseMapping.RFC1459)): ChannelState {
    return ChannelState(name = name, users = generateUsersFromNicks(nicks = listOf(), mappingState = mappingState))
}