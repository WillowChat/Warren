package engineer.carrot.warren.warren.state

data class IrcState(val connection: ConnectionState, val parsing: ParsingState)

data class ConnectionState(val server: String, val port: Int, val nickname: String, val username: String)

data class ParsingState(val userPrefixes: UserPrefixesState, val channelModes: ChannelModesState, val channelPrefixes: ChannelPrefixesState)

data class UserPrefixesState(var prefixesToModes: Map<Char, Char>)

data class ChannelModesState(var typeA: Set<Char>, var typeB: Set<Char>, var typeC: Set<Char>, var typeD: Set<Char>)

data class ChannelPrefixesState(val prefixes: Set<Char>)