package engineer.carrot.warren.warren

import engineer.carrot.warren.kale.Kale
import engineer.carrot.warren.kale.irc.message.IrcMessageSerialiser
import engineer.carrot.warren.kale.irc.message.rfc1459.PrivMsgMessage
import engineer.carrot.warren.kale.irc.message.utility.CaseMapping
import engineer.carrot.warren.warren.event.ChannelMessageEvent
import engineer.carrot.warren.warren.event.IWarrenEventDispatcher
import engineer.carrot.warren.warren.event.WarrenEventDispatcher
import engineer.carrot.warren.warren.event.internal.SendSomethingEvent
import engineer.carrot.warren.warren.state.*

data class ServerConfiguration(val server: String, val port: Int = 6697, val useTLS: Boolean = true, val fingerprints: Set<String>? = null)
data class UserConfiguration(val nickname: String, val password: String? = null, val sasl: Boolean = true)
data class ChannelsConfiguration(val channels: Map<String, String?> = mapOf())
data class EventConfiguration(val dispatcher: IWarrenEventDispatcher, val fireIncomingLineEvent: Boolean = false)

class WarrenFactory(val server: ServerConfiguration, val user: UserConfiguration, val channels: ChannelsConfiguration,
                    val events: EventConfiguration) {
    fun create(): IrcRunner {
        val lifecycleState = LifecycleState.CONNECTING
        val capLifecycleState = CapLifecycle.NEGOTIATING
        val capState = CapState(lifecycle = capLifecycleState, negotiate = setOf("multi-prefix", "sasl"), server = mapOf(), accepted = setOf(), rejected = setOf())

        var shouldAuth = false
        var saslLifecycleState = SaslLifecycle.NO_AUTH
        var saslCredentials: SaslCredentials? = null

        if (user.password != null && user.sasl) {
            shouldAuth = true
            saslLifecycleState = SaslLifecycle.AUTHING
            saslCredentials = SaslCredentials(account = user.nickname, password = user.password)
        }

        val saslState = SaslState(shouldAuth = shouldAuth, lifecycle = saslLifecycleState, credentials = saslCredentials)
        val connectionState = ConnectionState(server = server.server, port = server.port, nickname = user.nickname, username = user.nickname, lifecycle = lifecycleState, cap = capState, sasl = saslState)

        val kale = Kale().addDefaultMessages()
        val serialiser = IrcMessageSerialiser

        val socket = IrcSocket(connectionState.server, connectionState.port, server.useTLS, kale, serialiser, server.fingerprints)

        val userPrefixesState = UserPrefixesState(prefixesToModes = mapOf('@' to 'o', '+' to 'v'))
        val channelModesState = ChannelModesState(typeA = setOf('e', 'I', 'b'), typeB = setOf('k'), typeC = setOf('l'), typeD = setOf('i', 'm', 'n', 'p', 's', 't', 'S', 'r'))
        val channelPrefixesState = ChannelTypesState(types = setOf('#', '&'))
        val caseMappingState = CaseMappingState(mapping = CaseMapping.RFC1459)
        val parsingState = ParsingState(userPrefixesState, channelModesState, channelPrefixesState, caseMappingState)

        val joiningState = JoiningChannelsState(caseMappingState)
        for (channel in channels.channels) {
            joiningState += JoiningChannelState(channel.key, channel.value, status = JoiningChannelLifecycle.JOINING)
        }

        val channelsState = ChannelsState(joining = joiningState, joined = JoinedChannelsState(caseMappingState))

        val initialState = IrcState(connectionState, parsingState, channelsState)

        return IrcRunner(eventDispatcher = events.dispatcher, kale = kale, sink = socket, lineSource = socket, initialState = initialState, fireIncomingLineEvent = events.fireIncomingLineEvent)
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

        val factory = WarrenFactory(ServerConfiguration(server, port, useTLS), UserConfiguration(nickname, password, sasl = true),
                                    ChannelsConfiguration(mapOf("#carrot" to null, "#botdev" to null)), EventConfiguration(events, fireIncomingLineEvent = true))
        val connection = factory.create()

        events.on(ChannelMessageEvent::class) {
            LOGGER.info("channel message: $it")

            if (it.user.nick == "carrot" && it.message.equals("rabbit party", ignoreCase = true)) {
                connection.eventSink.add(SendSomethingEvent(PrivMsgMessage(target = it.channel, message = "🐰🎉"), connection.sink))
            }
        }

        connection.run()
    }
}