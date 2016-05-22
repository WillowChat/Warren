package engineer.carrot.warren.warren

import engineer.carrot.warren.kale.Kale
import engineer.carrot.warren.kale.irc.message.IrcMessageSerialiser
import engineer.carrot.warren.kale.irc.message.rfc1459.PrivMsgMessage
import engineer.carrot.warren.kale.irc.message.utility.CaseMapping
import engineer.carrot.warren.warren.state.*

object WarrenRunner {
    private val LOGGER = loggerFor<WarrenRunner>()

    fun createRunner(server: String, port: Int, useTLS: Boolean, nickname: String, password: String?, channels: Map<String, String?>, eventDispatcher: IWarrenEventDispatcher, fireIncomingLineEvent: Boolean, fingerprints: Set<String>? = null): IrcRunner {
        val lifecycleState = LifecycleState.CONNECTING
        val capLifecycleState = CapLifecycle.NEGOTIATING
        val capState = CapState(lifecycle = capLifecycleState, negotiate = setOf("multi-prefix", "sasl"), server = mapOf(), accepted = setOf(), rejected = setOf())

        var shouldAuth = false
        var saslLifecycleState = SaslLifecycle.NO_AUTH
        var saslCredentials: SaslCredentials? = null

        if (password != null) {
            shouldAuth = true
            saslLifecycleState = SaslLifecycle.AUTHING
            saslCredentials = SaslCredentials(account = nickname, password = password)
        }

        val saslState = SaslState(shouldAuth = shouldAuth, lifecycle = saslLifecycleState, credentials = saslCredentials)
        val connectionState = ConnectionState(server = server, port = port, nickname = nickname, username = nickname, lifecycle = lifecycleState, cap = capState, sasl = saslState)

        val kale = Kale().addDefaultMessages()
        val serialiser = IrcMessageSerialiser

        val socket = IrcSocket(connectionState.server, connectionState.port, useTLS, kale, serialiser, fingerprints)

        val userPrefixesState = UserPrefixesState(prefixesToModes = mapOf('@' to 'o', '+' to 'v'))
        val channelModesState = ChannelModesState(typeA = setOf('e', 'I', 'b'), typeB = setOf('k'), typeC = setOf('l'), typeD = setOf('i', 'm', 'n', 'p', 's', 't', 'S', 'r'))
        val channelPrefixesState = ChannelTypesState(types = setOf('#', '&'))
        val caseMappingState = CaseMappingState(mapping = CaseMapping.RFC1459)
        val parsingState = ParsingState(userPrefixesState, channelModesState, channelPrefixesState, caseMappingState)

        val joiningState = mutableMapOf<String, JoiningChannelState>()
        for (channel in channels) {
            joiningState += (channel.key to JoiningChannelState(channel.key, channel.value, status = JoiningChannelLifecycle.JOINING))
        }

        val channelsState = ChannelsState(joining = joiningState, joined = mutableMapOf())

        val initialState = IrcState(connectionState, parsingState, channelsState)

        return IrcRunner(eventDispatcher = eventDispatcher, kale = kale, sink = socket, lineSource = socket, initialState = initialState, fireIncomingLineEvent = fireIncomingLineEvent)
    }

    @JvmStatic fun main(args: Array<String>) {
        val server = args[0]
        val port = args[1].toInt()
        val nickname = args[2]
        val password = args.getOrNull(3)

        val eventDispatcher = WarrenEventDispatcher()
        eventDispatcher.onAnything {
            LOGGER.info("event: $it")
        }

        val connection = createRunner(server, port, (port != 6667), nickname, password, mapOf("#carrot" to null, "#botdev" to null), eventDispatcher, fireIncomingLineEvent = true)

        eventDispatcher.on(ChannelMessageEvent::class) {
            LOGGER.info("channel message: $it")

            if (it.user.nick == "carrot" && it.message.equals("rabbit party", ignoreCase = true)) {
                connection.eventSink.add(SendSomethingEvent(PrivMsgMessage(target = it.channel, message = "🐰🎉"), connection.sink))
            }
        }

        connection.run()
    }
}