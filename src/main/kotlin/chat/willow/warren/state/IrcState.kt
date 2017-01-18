package chat.willow.warren.state

import engineer.carrot.warren.kale.irc.message.utility.CaseMapping
import engineer.carrot.warren.kale.irc.prefix.Prefix

data class IrcState(val connection: ConnectionState, val parsing: ParsingState, val channels: ChannelsState)

data class ChannelsState(val joining: JoiningChannelsState, val joined: JoinedChannelsState)

interface INamed {
    val name: String
}

class JoinedChannelsState(mappingState: CaseMappingState) : CaseInsensitiveNamedMap<ChannelState>(mappingState)

class JoiningChannelsState(mappingState: CaseMappingState) : CaseInsensitiveNamedMap<JoiningChannelState>(mappingState)

data class JoiningChannelState(override val name: String, val key: String? = null, var status: JoiningChannelLifecycle) : INamed {
    override fun toString(): String {
        val key = if (key == null) { "null" } else { "***" }
        return "JoiningChannelState(name=$name, key=$key, status=$status)"
    }
}

enum class JoiningChannelLifecycle { JOINING, FAILED }

data class ChannelState(override val name: String, val users: ChannelUsersState, var topic: String? = null) : INamed

class ChannelUsersState(mappingState: CaseMappingState) : CaseInsensitiveNamedMap<ChannelUserState>(mappingState)

data class ChannelUserState(val prefix: Prefix, val account: String? = null, val awayMessage: String? = null, val modes: MutableSet<Char> = mutableSetOf()) : INamed {
    override val name = prefix.nick
}

data class ConnectionState(val server: String, val port: Int, var nickname: String, val user: String, val password: String? = null, var lifecycle: LifecycleState,
                           var nickServ: NickServState = NickServState(shouldAuth = false, lifecycle = AuthLifecycle.NO_AUTH, credentials = null, channelJoinWaitSeconds = 5),
                           var lastPingOrPong: Long = 0)

enum class LifecycleState { CONNECTING, REGISTERING, CONNECTED, DISCONNECTED }

data class NickServState(var shouldAuth: Boolean, var lifecycle: AuthLifecycle, var credentials: AuthCredentials?, val channelJoinWaitSeconds: Int)

enum class AuthLifecycle { NO_AUTH, AUTHING, AUTHED, AUTH_FAILED }

data class AuthCredentials(val account: String, val password: String) {
    override fun toString(): String {
        return "AuthCredentials(account=$account, password=***)"
    }
}

data class ParsingState(val userPrefixes: UserPrefixesState, val channelModes: ChannelModesState, val channelTypes: ChannelTypesState, val caseMapping: CaseMappingState)

data class UserPrefixesState(var prefixesToModes: Map<Char, Char>)

data class ChannelModesState(var typeA: Set<Char>, var typeB: Set<Char>, var typeC: Set<Char>, var typeD: Set<Char>)

data class ChannelTypesState(var types: Set<Char>)

data class CaseMappingState(var mapping: CaseMapping)