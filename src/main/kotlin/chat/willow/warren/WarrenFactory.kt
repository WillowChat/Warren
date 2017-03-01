package chat.willow.warren

import chat.willow.kale.Kale
import chat.willow.kale.KaleRouter
import chat.willow.kale.irc.message.IrcMessageSerialiser
import chat.willow.kale.irc.message.utility.CaseMapping
import chat.willow.kale.irc.tag.KaleTagRouter
import chat.willow.warren.event.IWarrenEventDispatcher
import chat.willow.warren.event.internal.NewLineWarrenEventGenerator
import chat.willow.warren.event.internal.WarrenInternalEventQueue
import chat.willow.warren.extension.cap.CapKeys
import chat.willow.warren.extension.cap.CapLifecycle
import chat.willow.warren.extension.cap.CapState
import chat.willow.warren.extension.monitor.MonitorState
import chat.willow.warren.extension.sasl.SaslState
import chat.willow.warren.helper.ThreadSleeper
import chat.willow.warren.helper.ThreadedExecutionContext
import chat.willow.warren.registration.RegistrationManager
import chat.willow.warren.state.*

data class ServerConfiguration(var server: String, var port: Int = 6697, var useTLS: Boolean = true, var fingerprints: Set<String>? = null, var password: String? = null)
data class UserConfiguration(var nickname: String, var user: String = nickname, var sasl: SaslConfiguration? = null, var nickserv: NickServConfiguration? = null) {}
data class SaslConfiguration(var account: String, var password: String)
data class NickServConfiguration(var account: String, var password: String, var channelJoinWaitSeconds: Int = 5)
data class ChannelsConfiguration(var channels: Map<String, String?> = mapOf())
data class EventsConfiguration(var fireIncomingLineEvent: Boolean = false)
data class ExtensionsConfiguration(var monitor: MonitorExtensionConfiguration = MonitorExtensionConfiguration(), var disabled: Set<CapKeys> = setOf())

data class MonitorExtensionConfiguration(var users: Set<String> = setOf())

interface IWarrenFactory {

    fun create(server: ServerConfiguration, user: UserConfiguration, channels: ChannelsConfiguration, events: EventsConfiguration, extensions: ExtensionsConfiguration, eventDispatcher: IWarrenEventDispatcher): IIrcConnection

}

object WarrenFactory: IWarrenFactory {

    override fun create(server: ServerConfiguration, user: UserConfiguration, channels: ChannelsConfiguration, events: EventsConfiguration, extensions: ExtensionsConfiguration, eventDispatcher: IWarrenEventDispatcher): IIrcConnection {
        val lifecycleState = LifecycleState.CONNECTING

        val capLifecycleState = CapLifecycle.NEGOTIATING
        val caps = CapKeys.values(). map { it.key }.toSet() - (extensions.disabled.map { it.key }.toSet())
        val capState = CapState(lifecycle = capLifecycleState, negotiate = caps, server = mapOf(), accepted = setOf(), rejected = setOf())

        val saslConfig = user.sasl
        val saslState = if (saslConfig != null) {
            val credentials = AuthCredentials(account = saslConfig.account, password = saslConfig.password)

            SaslState(shouldAuth = true, lifecycle = AuthLifecycle.AUTHING, credentials = credentials)
        } else {
            SaslState(shouldAuth = false, lifecycle = AuthLifecycle.NO_AUTH, credentials = null)
        }

        val nickservConfig = user.nickserv
        val nickServState = if (nickservConfig != null) {
            val credentials = AuthCredentials(account = nickservConfig.account, password = nickservConfig.password)

            NickServState(shouldAuth = true, lifecycle = AuthLifecycle.AUTHING, credentials = credentials, channelJoinWaitSeconds = nickservConfig.channelJoinWaitSeconds)
        } else {
            NickServState(shouldAuth = false, lifecycle = AuthLifecycle.NO_AUTH, credentials = null, channelJoinWaitSeconds = 5)
        }

        val connectionState = ConnectionState(server = server.server, port = server.port, nickname = user.nickname, user = user.user,
                lifecycle = lifecycleState, nickServ = nickServState, password = server.password)

        val kale = Kale(KaleRouter().useDefaults(), KaleTagRouter().useDefaults())
        val serialiser = IrcMessageSerialiser

        val socketFactory = if (server.useTLS) {
            TLSSocketFactory(connectionState.server, connectionState.port, server.fingerprints)
        } else {
            PlaintextSocketFactory(connectionState.server, connectionState.port)
        }

        val socket = IrcSocket(socketFactory, kale, serialiser)

        val userPrefixesState = UserPrefixesState(prefixesToModes = mapOf('@' to 'o', '+' to 'v'))
        val channelModesState = ChannelModesState(typeA = setOf('e', 'I', 'b'), typeB = setOf('k'), typeC = setOf('l'), typeD = setOf('i', 'm', 'n', 'p', 's', 't', 'S', 'r'))
        val channelPrefixesState = ChannelTypesState(types = setOf('#', '&'))
        val caseMappingState = CaseMappingState(mapping = CaseMapping.RFC1459)
        val parsingState = ParsingState(userPrefixesState, channelModesState, channelPrefixesState, caseMappingState)

        val joiningState = JoiningChannelsState(caseMappingState)
        for ((name, key) in channels.channels) {
            joiningState += JoiningChannelState(name, key, status = JoiningChannelLifecycle.JOINING)
        }

        val channelsState = ChannelsState(joining = joiningState, joined = JoinedChannelsState(caseMappingState))

        val initialMonitorState = MonitorState(maxCount = 0, users = extensions.monitor.users.toList())

        val initialState = IrcState(connectionState, parsingState, channelsState)

        val internalEventQueue = WarrenInternalEventQueue()
        val newLineGenerator = NewLineWarrenEventGenerator(internalEventQueue, kale, lineSource = socket, fireIncomingLineEvent = events.fireIncomingLineEvent, warrenEventDispatcher = eventDispatcher)

        val registrationManager = RegistrationManager()

        val pingExecutionContext = ThreadedExecutionContext(name = "ping context")
        val newLineExecutionContext = ThreadedExecutionContext(name = "new line context")

        val runner = IrcConnection(eventDispatcher = eventDispatcher, internalEventQueue = internalEventQueue, newLineGenerator = newLineGenerator, kale = kale, sink = socket, initialState = initialState, initialCapState = capState, initialSaslState = saslState, initialMonitorState = initialMonitorState, registrationManager = registrationManager, sleeper = ThreadSleeper, pingGeneratorExecutionContext = pingExecutionContext, lineGeneratorExecutionContext = newLineExecutionContext)

        registrationManager.listener = runner

        return runner
    }

}