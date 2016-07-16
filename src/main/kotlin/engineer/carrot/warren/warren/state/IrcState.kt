package engineer.carrot.warren.warren.state

import engineer.carrot.warren.kale.irc.message.utility.CaseMapping

data class IrcState(val connection: ConnectionState, val parsing: ParsingState, val channels: ChannelsState)

data class ChannelsState(val joining: JoiningChannelsState, val joined: JoinedChannelsState)

interface INamed { val name: String }

open class CaseInsensitiveNamedMap<NamedType : INamed>(var mappingState: CaseMappingState) {
    private val namedThings = mutableMapOf<String, NamedType>()

    operator fun get(key: String): NamedType? {
        return namedThings[mappingState.mapping.toLower(key)]
    }

    fun put(value: NamedType) {
        namedThings[mappingState.mapping.toLower(value.name)] = value
    }

    operator fun minusAssign(key: String) {
        remove(key)
    }

    fun remove(key: String): NamedType? {
        return namedThings.remove(mappingState.mapping.toLower(key))
    }

    operator fun plusAssign(namedThing: NamedType) {
        put(namedThing)
    }

    operator fun plusAssign(namedThings: Collection<NamedType>) {
        for (namedThing in namedThings) {
            put(namedThing)
        }
    }

    fun contains(key: String): Boolean {
        return namedThings.contains(mappingState.mapping.toLower(key))
    }

    val all: Map<String, NamedType>
        get() = namedThings

    override fun equals(other: Any?): Boolean {
        if (other !is CaseInsensitiveNamedMap<*>) {
            return false
        }

        return namedThings == other.namedThings && mappingState == other.mappingState
    }

    override fun hashCode(): Int{
        var result = mappingState.hashCode()
        result = 31 * result + namedThings.hashCode()
        return result
    }

    override fun toString(): String{
        return "CaseInsensitiveNamedMap(mappingState=$mappingState, namedThings=$namedThings)"
    }

}

class JoinedChannelsState(mappingState: CaseMappingState): CaseInsensitiveNamedMap<ChannelState>(mappingState)

class JoiningChannelsState(mappingState: CaseMappingState): CaseInsensitiveNamedMap<JoiningChannelState>(mappingState)

data class JoiningChannelState(override val name: String, val key: String? = null, var status: JoiningChannelLifecycle): INamed {
    override fun toString(): String {
        return "JoiningChannelState(name=$name, key=${if (key == null) {
            "null"
        } else {
            "***"
        }}, status=$status)"
    }
}

enum class JoiningChannelLifecycle { JOINING, FAILED }

data class ChannelState(override val name: String, val users: ChannelUsersState, var topic: String? = null) : INamed

class ChannelUsersState(mappingState: CaseMappingState): CaseInsensitiveNamedMap<ChannelUserState>(mappingState)

data class ChannelUserState(val nick: String, val modes: MutableSet<Char> = mutableSetOf()): INamed {
    override val name = nick
}

data class ConnectionState(val server: String, val port: Int, var nickname: String, val user: String, var lifecycle: LifecycleState, val cap: CapState,
                           val sasl: SaslState = SaslState(shouldAuth = false, lifecycle = AuthLifecycle.NO_AUTH, credentials = null),
                           var nickServ: NickServState = NickServState(shouldAuth = false, lifecycle = AuthLifecycle.NO_AUTH, credentials = null, channelJoinWaitSeconds = 5),
                           var lastPingOrPong: Long = 0)

enum class LifecycleState { CONNECTING, REGISTERING, CONNECTED, DISCONNECTED }

data class CapState(var lifecycle: CapLifecycle, var negotiate: Set<String>, var server: Map<String, String?>, var accepted: Set<String>, var rejected: Set<String>)

enum class CapLifecycle { NEGOTIATING, NEGOTIATED, FAILED }

data class SaslState(var shouldAuth: Boolean, var lifecycle: AuthLifecycle, var credentials: AuthCredentials?)

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