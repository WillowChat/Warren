package engineer.carrot.warren.warren.integration

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import engineer.carrot.warren.kale.IKale
import engineer.carrot.warren.kale.Kale
import engineer.carrot.warren.kale.irc.message.utility.CaseMapping
import engineer.carrot.warren.warren.ILineSource
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.IrcRunner
import engineer.carrot.warren.warren.event.IWarrenEventDispatcher
import engineer.carrot.warren.warren.event.internal.*
import engineer.carrot.warren.warren.state.*
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue

class SanityCheckIntegrationTests {

    lateinit var runner: IrcRunner
    lateinit var connectionState: ConnectionState
    lateinit var channelModesState: ChannelModesState
    lateinit var userPrefixesState: UserPrefixesState

    lateinit var mockEventDispatcher: IWarrenEventDispatcher
    lateinit var mockSink: IMessageSink
    lateinit var mockLineSource: ILineSource
    lateinit var mockNewLineGenerator: IWarrenInternalEventGenerator

    lateinit var internalEventQueue: IntegrationTestLineGenerator
    lateinit var kale: IKale

    @Before fun setUp() {
        val lifecycleState = LifecycleState.CONNECTING
        val capLifecycleState = CapLifecycle.NEGOTIATED
        val capState = CapState(lifecycle = capLifecycleState, negotiate = setOf(), server = mapOf(), accepted = setOf(), rejected = setOf())
        connectionState = ConnectionState(server = "test.server", port = 6697, nickname = "test-nick", user = "test-nick", lifecycle = lifecycleState, cap = capState)

        userPrefixesState = UserPrefixesState(prefixesToModes = mapOf('@' to 'o', '+' to 'v'))
        channelModesState = ChannelModesState(typeA = setOf('e', 'I', 'b'), typeB = setOf('k'), typeC = setOf('l'), typeD = setOf('i', 'm', 'n', 'p', 's', 't', 'S', 'r'))
        val channelPrefixesState = ChannelTypesState(types = setOf('#', '&'))
        val caseMappingState = CaseMappingState(mapping = CaseMapping.RFC1459)
        val parsingState = ParsingState(userPrefixesState, channelModesState, channelPrefixesState, caseMappingState)
        val channelsState = ChannelsState(joining = JoiningChannelsState(caseMappingState), joined = JoinedChannelsState(caseMappingState))

        val initialState = IrcState(connectionState, parsingState, channelsState)

        mockEventDispatcher = mock()
        mockNewLineGenerator = mock()

        kale = Kale().addDefaultMessages()
        internalEventQueue = IntegrationTestLineGenerator(queueOf(), kale)

        mockSink = mock()
        mockLineSource = mock()

        runner = IrcRunner(mockEventDispatcher, internalEventQueue, mockNewLineGenerator, kale, mockSink, initialState, startAsyncThreads = false)
    }

    @Test fun test_run_ImaginaryNet_RegistrationAndMOTD_ResultsInConnectedLifecycle_WithCorrectCAPs() {
        internalEventQueue.lines = queueOf(
                "NOTICE AUTH :*** Processing connection to imaginary.bunnies.io",
                "NOTICE AUTH :*** Looking up your hostname...",
                "NOTICE AUTH :*** Checking Ident",
                "NOTICE AUTH :*** Found your hostname",
                "NOTICE AUTH :*** No Ident response",
                ":imaginary.bunnies.io CAP * LS :multi-prefix",
                "PING :61BD1F40",
                ":imaginary.bunnies.io CAP carrot-warren ACK :multi-prefix",
                ":imaginary.bunnies.io 001 carrot-warren :Welcome to the ImaginaryNet Internet Relay Chat Network carrot-warren",
                ":imaginary.bunnies.io 002 carrot-warren :Your host is imaginary.bunnies.io[123.456.789.012/6697], running version ircd-ratbox-3.0.8+sa4",
                ":imaginary.bunnies.io 003 carrot-warren :This server was created Mon Feb 1 2016 at 21:30:59 GMT",
                ":imaginary.bunnies.io 004 carrot-warren imaginary.bunnies.io ircd-ratbox-3.0.8+sa4 oiwszcrkfydnxbauglZCD biklmnopstveIrS bkloveI",
                ":imaginary.bunnies.io 005 carrot-warren CHANTYPES=&# EXCEPTS INVEX CHANMODES=eIb,k,l,imnpstSr CHANLIMIT=&#:50 PREFIX=(ov)@+ MAXLIST=beI:50 MODES=4 NETWORK=ImaginaryNet KNOCK STATUSMSG=@+ CALLERID=g :are supported by this server",
                ":imaginary.bunnies.io 005 carrot-warren SAFELIST ELIST=U CASEMAPPING=rfc1459 CHARSET=ascii NICKLEN=30 CHANNELLEN=50 TOPICLEN=390 ETRACE CPRIVMSG CNOTICE DEAF=D MONITOR=100 :are supported by this server",
                ":imaginary.bunnies.io 005 carrot-warren FNC TARGMAX=NAMES:1,LIST:1,KICK:1,WHOIS:1,PRIVMSG:1,NOTICE:1,ACCEPT:,MONITOR: :are supported by this server",
                ":imaginary.bunnies.io 251 carrot-warren :There are 67 users and 61 invisible on 6 servers",
                ":imaginary.bunnies.io 252 carrot-warren 14 :IRC Operators online",
                ":imaginary.bunnies.io 254 carrot-warren 99 :channels formed",
                ":imaginary.bunnies.io 255 carrot-warren :I have 17 clients and 2 servers",
                ":imaginary.bunnies.io 265 carrot-warren 17 19 :Current local users 17, max 19",
                ":imaginary.bunnies.io 266 carrot-warren 128 137 :Current global users 128, max 137",
                ":imaginary.bunnies.io 250 carrot-warren :Highest connection count: 21 (19 clients) (1139 connections received)",
                ":imaginary.bunnies.io 375 carrot-warren :- imaginary.bunnies.io Message of the Day -",
                ":imaginary.bunnies.io 372 carrot-warren :-  no motd",
                ":imaginary.bunnies.io 376 carrot-warren :End of /MOTD command."
        )

        whenever(mockSink.setUp()).thenReturn(true)

        runner.run()

        assertEquals(LifecycleState.CONNECTED, runner.lastStateSnapshot!!.connection.lifecycle)
        assertEquals(setOf("multi-prefix"), runner.lastStateSnapshot!!.connection.cap.accepted)
    }

}

fun <T> queueOf(vararg elements: T): Queue<T> {
    val queue = LinkedBlockingQueue<T>()

    for (element in elements) {
        queue.add(element)
    }

    return queue
}

class IntegrationTestLineGenerator(var lines: Queue<String>, private val kale: IKale): IWarrenInternalEventQueue {

    override fun grab(): IWarrenInternalEvent? {
        val nextLine = lines.poll() ?: return null

        return NewLineEvent(nextLine, kale)
    }

    override fun add(event: IWarrenInternalEvent) {
        // NO-OP
    }

    override fun add(closure: () -> Unit) {
        // NO-OP
    }

    override fun clear() {
        // NO-OP
    }

}

