package chat.willow.warren

import chat.willow.kale.Kale
import chat.willow.kale.KaleRouter
import chat.willow.kale.irc.message.IrcMessageSerialiser
import chat.willow.kale.irc.message.rfc1459.PrivMsgMessage
import chat.willow.kale.irc.message.utility.CaseMapping
import chat.willow.warren.event.ChannelMessageEvent
import chat.willow.warren.event.IWarrenEventDispatcher
import chat.willow.warren.event.WarrenEventDispatcher
import chat.willow.warren.event.internal.NewLineWarrenEventGenerator
import chat.willow.warren.event.internal.SendSomethingEvent
import chat.willow.warren.event.internal.WarrenInternalEventQueue
import chat.willow.warren.extension.cap.CapKeys
import chat.willow.warren.extension.cap.CapLifecycle
import chat.willow.warren.extension.cap.CapState
import chat.willow.warren.extension.sasl.SaslState
import chat.willow.warren.helper.ThreadSleeper
import chat.willow.warren.helper.ThreadedExecutionContext
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.registration.RegistrationManager
import chat.willow.warren.state.*

data class ServerConfiguration(val server: String, val port: Int = 6697, val useTLS: Boolean = true, val fingerprints: Set<String>? = null, val password: String? = null)
data class UserConfiguration(val nickname: String, val user: String = nickname, val sasl: SaslConfiguration? = null, val nickserv: NickServConfiguration? = null)
data class SaslConfiguration(val account: String, val password: String)
data class NickServConfiguration(val account: String, val password: String, val channelJoinWaitSeconds: Int = 5)
data class ChannelsConfiguration(val channels: Map<String, String?> = mapOf())
data class EventConfiguration(val dispatcher: IWarrenEventDispatcher, val fireIncomingLineEvent: Boolean = false)

class WarrenFactory(val server: ServerConfiguration, val user: UserConfiguration, val channels: ChannelsConfiguration, val events: EventConfiguration) {

    fun create(): IrcConnection {
        val lifecycleState = LifecycleState.CONNECTING

        val capLifecycleState = CapLifecycle.NEGOTIATING
        val capState = CapState(lifecycle = capLifecycleState, negotiate = setOf(CapKeys.CAP_NOTIFY.key, CapKeys.MULTI_PREFIX.key, CapKeys.SASL.key, CapKeys.ACCOUNT_NOTIFY.key, CapKeys.AWAY_NOTIFY.key, CapKeys.EXTENDED_JOIN.key), server = mapOf(), accepted = setOf(), rejected = setOf())

        val saslState = if (user.sasl != null) {
            val credentials = AuthCredentials(account = user.sasl.account, password = user.sasl.password)

            SaslState(shouldAuth = true, lifecycle = AuthLifecycle.AUTHING, credentials = credentials)
        } else {
            SaslState(shouldAuth = false, lifecycle = AuthLifecycle.NO_AUTH, credentials = null)
        }

        val nickServState = if (user.nickserv != null) {
            val credentials = AuthCredentials(account = user.nickserv.account, password = user.nickserv.password)

            NickServState(shouldAuth = true, lifecycle = AuthLifecycle.AUTHING, credentials = credentials, channelJoinWaitSeconds = user.nickserv.channelJoinWaitSeconds)
        } else {
            NickServState(shouldAuth = false, lifecycle = AuthLifecycle.NO_AUTH, credentials = null, channelJoinWaitSeconds = 5)
        }

        val connectionState = ConnectionState(server = server.server, port = server.port, nickname = user.nickname, user = user.user,
                lifecycle = lifecycleState, nickServ = nickServState, password = server.password)

        val kale = Kale(KaleRouter().useDefaults())
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

        val initialState = IrcState(connectionState, parsingState, channelsState)

        val internalEventQueue = WarrenInternalEventQueue()
        val newLineGenerator = NewLineWarrenEventGenerator(internalEventQueue, kale, lineSource = socket, fireIncomingLineEvent = events.fireIncomingLineEvent, warrenEventDispatcher = events.dispatcher)

        val registrationManager = RegistrationManager()

        val pingExecutionContext = ThreadedExecutionContext(name = "ping context")
        val newLineExecutionContext = ThreadedExecutionContext(name = "new line context")

        val runner = IrcConnection(eventDispatcher = events.dispatcher, internalEventQueue = internalEventQueue, newLineGenerator = newLineGenerator, kale = kale, sink = socket, initialState = initialState, initialCapState = capState, initialSaslState = saslState, registrationManager = registrationManager, sleeper = ThreadSleeper, pingGeneratorExecutionContext = pingExecutionContext, lineGeneratorExecutionContext = newLineExecutionContext)

        registrationManager.listener = runner

        return runner
    }

}

object WarrenRunner {

    private val LOGGER = loggerFor<WarrenRunner>()

    @JvmStatic fun main(args: Array<String>) {
        val server = args[0]
        val port = args[1].toInt()
        val useTLS = (port != 6667)
        val nickname = args[2]
        val password = args.getOrNull(3)

        val events = WarrenEventDispatcher()
        events.onAnything {
            LOGGER.info("event: $it")
        }

        val sasl = if (password != null) {
            SaslConfiguration(account = nickname, password = password)
        } else {
            null
        }

        val factory = WarrenFactory(ServerConfiguration(server, port, useTLS), UserConfiguration(nickname, sasl = sasl),
                ChannelsConfiguration(mapOf("#carrot" to null, "#botdev" to null)), EventConfiguration(events, fireIncomingLineEvent = true))
        val connection = factory.create()

        events.on(ChannelMessageEvent::class) {
            LOGGER.info("channel message: $it")

            if (it.user.prefix.nick == "carrot" && it.message.equals("rabbit party", ignoreCase = true)) {
                connection.eventSink.add(SendSomethingEvent(PrivMsgMessage(target = it.channel.name, message = "🐰🎉"), connection.sink))
            }
        }

        connection.run()
    }

}