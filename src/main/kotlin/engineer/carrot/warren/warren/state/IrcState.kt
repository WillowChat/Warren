package engineer.carrot.warren.warren.state

data class IrcState(val connection: ConnectionState, val parsing: ParsingState, val channels: ChannelsState)

data class ChannelsState(val joined: MutableMap<String, ChannelState>)

data class ChannelState(val name: String, val users: MutableMap<String, ChannelUserState>, var topic: String? = null)

data class ChannelUserState(val nick: String, val modes: Set<Char> = setOf())

data class ConnectionState(val server: String, val port: Int, var nickname: String, val username: String, var lifecycle: LifecycleState)

enum class LifecycleState { CONNECTING, CONNECTED, DISCONNECTED }

data class ParsingState(val userPrefixes: UserPrefixesState, val channelModes: ChannelModesState, val channelTypes: ChannelTypesState)

data class UserPrefixesState(var prefixesToModes: Map<Char, Char>)

data class ChannelModesState(var typeA: Set<Char>, var typeB: Set<Char>, var typeC: Set<Char>, var typeD: Set<Char>)

data class ChannelTypesState(var types: Set<Char>)