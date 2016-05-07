package engineer.carrot.warren.warren

import engineer.carrot.warren.kale.Kale
import engineer.carrot.warren.kale.irc.message.IrcMessageSerialiser
import engineer.carrot.warren.warren.state.*

object WarrenRunner {
    @JvmStatic fun main(args: Array<String>) {
        val server = args[0]
        val port = args[1].toInt()
        val nickname = args[2]
        val password = args.getOrNull(3)

        val lifecycleState = LifecycleState.CONNECTING
        val capLifecycleState = CapLifecycle.NEGOTIATING
        val capState = CapState(lifecycle = capLifecycleState, negotiate = setOf("multi-prefix", "sasl", "account-notify", "away-notify", "extended-join", "account-tag"), server = mapOf(), accepted = setOf(), rejected = setOf())

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

        val socket = IrcSocket(connectionState.server, connectionState.port, kale, serialiser)

        if (!socket.setUp()) {
            println("failed to set up irc socket for: ${connectionState}")
            return
        }

        val userPrefixesState = UserPrefixesState(prefixesToModes = mapOf('@' to 'o', '+' to 'v'))
        val channelModesState = ChannelModesState(typeA = setOf('e', 'I', 'b'), typeB = setOf('k'), typeC = setOf('l'), typeD = setOf('i', 'm', 'n', 'p', 's', 't', 'S', 'r'))
        val channelPrefixesState = ChannelTypesState(types = setOf('#', '&'))
        val parsingState = ParsingState(userPrefixesState, channelModesState, channelPrefixesState)

        val channelsState = ChannelsState(joined = mutableMapOf())

        val initialState = IrcState(connectionState, parsingState, channelsState)

        val eventDispatcher = WarrenEventDispatcher
        eventDispatcher.onAnythingListeners += {
            println("event: $it")
        }

        val connection = IrcRunner(eventDispatcher = eventDispatcher, kale = kale, sink = socket, processor = socket, initialState = initialState)
        connection.run()

        socket.tearDown()
    }
}