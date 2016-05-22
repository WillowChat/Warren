package engineer.carrot.warren.warren.state

import engineer.carrot.warren.kale.irc.message.utility.CaseMapping

data class IrcState(val connection: ConnectionState, val parsing: ParsingState, val channels: ChannelsState)

data class ChannelsState(val joining: JoiningChannelsState, val joined: JoinedChannelsState)

interface IChannelNameOwning { val name: String }

open class ChannelWrangler<ChannelType: IChannelNameOwning>(var mappingState: CaseMappingState) {
    private val channels = mutableMapOf<String, ChannelType>()

    operator fun get(key: String): ChannelType? {
        return channels[mappingState.mapping.toLower(key)]
    }

    fun put(value: ChannelType) {
        channels[mappingState.mapping.toLower(value.name)] = value
    }

    operator fun minusAssign(key: String) {
        remove(key)
    }

    fun remove(key: String): ChannelType? {
        return channels.remove(mappingState.mapping.toLower(key))
    }

    operator fun plusAssign(channel: ChannelType) {
        put(channel)
    }

    operator fun plusAssign(channels: Collection<ChannelType>) {
        for (channel in channels) {
            put(channel)
        }
    }

    fun contains(key: String): Boolean {
        return channels.contains(mappingState.mapping.toLower(key))
    }

    val all: Map<String, ChannelType>
        get() = channels

    override fun equals(other: Any?): Boolean {
        if (other !is ChannelWrangler<*>) {
            return false
        }

        return channels == other.channels && mappingState == other.mappingState
    }

    override fun hashCode(): Int{
        var result = mappingState.hashCode()
        result = 31 * result + channels.hashCode()
        return result
    }
}

class JoinedChannelsState(mappingState: CaseMappingState): ChannelWrangler<ChannelState>(mappingState)

class JoiningChannelsState(mappingState: CaseMappingState): ChannelWrangler<JoiningChannelState>(mappingState)

data class JoiningChannelState(override val name: String, val key: String? = null, var status: JoiningChannelLifecycle): IChannelNameOwning {
    override fun toString(): String {
        return "JoiningChannelState(name=$name, key=${if (key == null) {
            "null"
        } else {
            "***"
        }}, status=$status)"
    }
}

enum class JoiningChannelLifecycle { JOINING, FAILED }

data class ChannelState(override val name: String, val users: MutableMap<String, ChannelUserState>, var topic: String? = null) : IChannelNameOwning

data class ChannelUserState(val nick: String, val modes: MutableSet<Char> = mutableSetOf())

data class ConnectionState(val server: String, val port: Int, var nickname: String, val username: String, var lifecycle: LifecycleState, val cap: CapState, val sasl: SaslState)

enum class LifecycleState { CONNECTING, REGISTERING, CONNECTED, DISCONNECTED }

data class CapState(var lifecycle: CapLifecycle, var negotiate: Set<String>, var server: Map<String, String?>, var accepted: Set<String>, var rejected: Set<String>)

enum class CapLifecycle { NEGOTIATING, NEGOTIATED, FAILED }

data class SaslState(var shouldAuth: Boolean, var lifecycle: SaslLifecycle, var credentials: SaslCredentials?)

enum class SaslLifecycle { NO_AUTH, AUTHING, AUTHED, AUTH_FAILED }

data class SaslCredentials(val account: String, val password: String) {
    override fun toString(): String {
        return "SaslCredentials(account=$account, password=***)"
    }
}

data class ParsingState(val userPrefixes: UserPrefixesState, val channelModes: ChannelModesState, val channelTypes: ChannelTypesState, val caseMapping: CaseMappingState)

data class UserPrefixesState(var prefixesToModes: Map<Char, Char>)

data class ChannelModesState(var typeA: Set<Char>, var typeB: Set<Char>, var typeC: Set<Char>, var typeD: Set<Char>)

data class ChannelTypesState(var types: Set<Char>)

data class CaseMappingState(var mapping: CaseMapping)