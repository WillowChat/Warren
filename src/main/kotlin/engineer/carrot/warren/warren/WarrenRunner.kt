package engineer.carrot.warren.warren

import engineer.carrot.warren.kale.Kale
import engineer.carrot.warren.kale.irc.message.IrcMessageSerialiser
import engineer.carrot.warren.warren.state.*

object WarrenRunner {
    @JvmStatic fun main(args: Array<String>) {
        val lifecycleState = LifecycleState.CONNECTING
        val capLifecycleState = CapLifecycle.NEGOTIATING
        val capState = CapState(lifecycle = capLifecycleState, negotiate = setOf("multi-prefix", "sasl", "account-notify", "away-notify", "extended-join", "account-tag"), server = mapOf(), accepted = setOf(), rejected = setOf())
        val connectionState = ConnectionState(server = args[0], port = args[1].toInt(), nickname = args[2], username = args[2], lifecycle = lifecycleState, cap = capState)

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

        val connection = IrcRunner(kale = kale, sink = socket, processor = socket, initialState = initialState)
        connection.run()

        socket.tearDown()
    }
}