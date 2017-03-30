package chat.willow.warren

import chat.willow.kale.irc.CharacterCodes
import chat.willow.kale.irc.message.rfc1459.*
import chat.willow.warren.event.IWarrenEventDispatcher
import chat.willow.warren.event.WarrenEventDispatcher
import chat.willow.warren.extension.cap.CapKeys
import chat.willow.warren.state.*

interface IWarrenClient: IClientMessageSending, IStringMessageSending {

    val state: IrcState
    val events: IWarrenEventDispatcher
    val channels: IWarrenChannels
    val nick: String

    fun join(channel: String)
    fun join(channel: String, key: String)
    fun leave(channel: String)

    fun start()
}

interface IStringMessageSending {

    fun send(message: String)

}

interface ITargetedMessageSending {

    fun send(message: String, target: String)

}

interface ITypedMessageSending {

    fun <M: Any> send(message: M)

}

interface IClientMessageSending: ITargetedMessageSending, ITypedMessageSending

data class WarrenChannelUserModes(private val state: Set<Char>, private val user: WarrenChannelUser, private val channel: IWarrenChannel) {

    operator fun contains(mode: Char): Boolean {
        return state.contains(mode)
    }

    operator fun plusAssign(mode: Char) {
        channel.addMode(mode, user.nick)
    }

    operator fun minusAssign(mode: Char) {
        channel.removeMode(mode, user.nick)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is WarrenChannelUserModes) return false
        return state == other.state && user == other.user
    }

    override fun hashCode(): Int {
        return state.hashCode() + 31 * user.hashCode()
    }

}

data class WarrenChannelUser(private val state: ChannelUserState, private val channel: IWarrenChannel): IStringMessageSending {

    val prefix get() = state.prefix
    val nick get() = prefix.nick

    val modes get() = WarrenChannelUserModes(state.modes, user = this, channel = channel)

    override fun send(message: String) {
        channel.send("${state.name}: $message")
    }

    fun kick() {
        channel.kick(nick)
    }

}

data class WarrenChannelUsers(private val state: ChannelUsersState, private val channel: IWarrenChannel) {

    operator fun get(user: String): WarrenChannelUser? {
        val userState = state[user] ?: return null

        return WarrenChannelUser(userState, channel = channel)
    }

}

interface IWarrenChannel: IStringMessageSending {
    fun addMode(mode: Char, user: String)
    fun removeMode(mode: Char, user: String)
    fun kick(user: String)
    fun invite(user: String)
}

data class WarrenChannel(private val state: ChannelState, private val client: IClientMessageSending): IWarrenChannel {

    val name get() = state.name
    val users get() = WarrenChannelUsers(state.users, channel = this)
    val topic get() = state.topic

    override fun send(message: String) {
        client.send(message, target = name)
    }

    override fun addMode(mode: Char, user: String) {
        val modifier = ModeMessage.ModeModifier(type = CharacterCodes.PLUS, mode = mode, parameter = user)
        client.send(ModeMessage.Command(target = name, modifiers = listOf(modifier)))
    }

    override fun removeMode(mode: Char, user: String) {
        val modifier = ModeMessage.ModeModifier(type = CharacterCodes.MINUS, mode = mode, parameter = user)
        client.send(ModeMessage.Command(target = name, modifiers = listOf(modifier)))
    }

    override fun kick(user: String) {
        client.send(KickMessage.Command(users = listOf(user), channels = listOf(name)))
    }

    override fun invite(user: String) {
        client.send(InviteMessage.Command(user = user, channel = name))
    }

    override fun equals(other: Any?): Boolean {
        if (other !is WarrenChannel) return false
        return state == other.state
    }

    override fun hashCode(): Int {
        return state.hashCode()
    }
}

interface IWarrenChannels {

    val all: List<IWarrenChannel>
    operator fun get(channel: String): IWarrenChannel?

}

data class WarrenChannels(private val state: JoinedChannelsState, private val client: IClientMessageSending): IWarrenChannels {

    override val all get() = state.all.keys.map { this[it] }.filterNotNull()

    override operator fun get(channel: String): IWarrenChannel? {
        val channelState = state[channel] ?: return null

        return WarrenChannel(channelState, client = client)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is WarrenChannels) return false
        return state == other.state
    }

    override fun hashCode(): Int {
        return state.hashCode()
    }

}

class WarrenClient constructor(private val connection: IIrcConnection, override val events: IWarrenEventDispatcher): IWarrenClient {

    override val state get() = connection.state
    override val channels get() = WarrenChannels(connection.state.channels.joined, client = connection)
    override val nick get() = connection.state.connection.nickname

    override fun start() {
        connection.start()
    }

    override fun join(channel: String) {
        connection.send(JoinMessage.Command(channels = listOf(channel)))
    }

    override fun join(channel: String, key: String) {
        connection.send(JoinMessage.Command(channels = listOf(channel), keys = listOf(key)))
    }

    override fun leave(channel: String) {
        connection.send(PartMessage.Command(channels = listOf(channel)))
    }

    override fun <M : Any> send(message: M) {
        connection.send(message)
    }

    override fun send(message: String, target: String) {
        connection.send(message, target)
    }

    override fun send(message: String) {
        connection.send(message)
    }

    companion object {
        fun build(factory: IWarrenFactory = WarrenFactory, init: Builder.() -> Unit) = Builder(factory, init).build()
    }

    class Builder private constructor() {

        private lateinit var factory: IWarrenFactory
        lateinit var server: ServerConfiguration
        lateinit var user: UserConfiguration
        var channels = ChannelsConfiguration()
        var extensions = ExtensionsConfiguration()
        var events = EventsConfiguration()
        var eventDispatcher = WarrenEventDispatcher()

        constructor(factory: IWarrenFactory, init: Builder.() -> Unit) : this() {
            this.factory = factory
            init()
        }

        fun build(): IWarrenClient {
            val connection = factory.create(server, user, channels, events, extensions, eventDispatcher)

            return WarrenClient(connection, eventDispatcher)
        }

        fun server(server: String, init: ServerConfiguration.() -> Unit = {}) = apply {
            this.server = ServerConfiguration(server).apply { init() }
        }

        fun user(nickname: String, init: UserConfigurationBuilder.() -> Unit = {}) = apply {
            this.user = UserConfigurationBuilder(nickname, init).build()
        }

        fun channel(name: String) = apply {
            this.channels.channels += name to null
        }

        fun channel(nameKey: Pair<String, String>) = apply {
            this.channels.channels += nameKey
        }

        fun extensions(init: ExtensionsConfigurationBuilder.() -> Unit = {}) = apply {
            this.extensions = ExtensionsConfigurationBuilder(init).build()
        }

        fun events(init: EventsConfiguration.() -> Unit = {}) = apply {
            this.events = EventsConfiguration().apply { init() }
        }
    }

}

class UserConfigurationBuilder private constructor() {

    private lateinit var nickname: String
    private var user: String? = null
    private var sasl: SaslConfiguration? = null
    private var nickserv: NickServConfiguration? = null

    constructor(nickname: String, init: UserConfigurationBuilder.() -> Unit) : this() {
        this.nickname = nickname
        init()
    }

    fun sasl(userPassword: Pair<String, String>) = apply {
        this.sasl = SaslConfiguration(account = userPassword.first, password = userPassword.second)
    }

    fun nickserv(userPassword: Pair<String, String>, init: NickServConfiguration.() -> Unit = {}) = apply {
        this.nickserv = NickServConfiguration(account = userPassword.first, password = userPassword.second).apply { init() }
    }

    fun build(): UserConfiguration {
        val user = user ?: nickname

        return UserConfiguration(nickname, user, sasl, nickserv)
    }
}

class ExtensionsConfigurationBuilder private constructor() {

    private var monitor = MonitorExtensionConfiguration()
    private var disabled: Set<CapKeys> = setOf()

    constructor(init: ExtensionsConfigurationBuilder.() -> Unit) : this() {
        init()
    }

    fun monitor(vararg users: String) = apply {
        this.monitor.users += users
    }

    fun disable(vararg caps: CapKeys) = apply {
        this.disabled += caps.toSet()
    }

    fun build(): ExtensionsConfiguration {
        return ExtensionsConfiguration(monitor, disabled)
    }
}