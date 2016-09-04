package engineer.carrot.warren.warren

import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import engineer.carrot.warren.kale.IKale
import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.IKaleParsingStateDelegate
import engineer.carrot.warren.kale.irc.message.IMessage
import engineer.carrot.warren.kale.irc.message.IrcMessage
import engineer.carrot.warren.kale.irc.message.rfc1459.NickMessage
import engineer.carrot.warren.kale.irc.message.rfc1459.UserMessage
import engineer.carrot.warren.kale.irc.message.utility.CaseMapping
import engineer.carrot.warren.warren.event.IWarrenEventDispatcher
import engineer.carrot.warren.warren.event.internal.IWarrenInternalEventGenerator
import engineer.carrot.warren.warren.event.internal.IWarrenInternalEventQueue
import engineer.carrot.warren.warren.handler.*
import engineer.carrot.warren.warren.handler.rpl.*
import engineer.carrot.warren.warren.handler.rpl.Rpl005.Rpl005Handler
import engineer.carrot.warren.warren.handler.sasl.AuthenticateHandler
import engineer.carrot.warren.warren.handler.sasl.Rpl903Handler
import engineer.carrot.warren.warren.handler.sasl.Rpl904Handler
import engineer.carrot.warren.warren.handler.sasl.Rpl905Handler
import engineer.carrot.warren.warren.state.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations

class IrcRunnerTests {
    lateinit var runner: IrcRunner
    lateinit var connectionState: ConnectionState
    lateinit var channelModesState: ChannelModesState
    lateinit var userPrefixesState: UserPrefixesState

    lateinit var mockEventDispatcher: IWarrenEventDispatcher
    lateinit var mockInternalEventQueue: IWarrenInternalEventQueue
    lateinit var mockNewLineGenerator: IWarrenInternalEventGenerator
    lateinit var mockKale: MockKale
    lateinit var mockSink: IMessageSink
    lateinit var mockLineSource: ILineSource

    @Before fun setUp() {
        val lifecycleState = LifecycleState.DISCONNECTED
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
        mockInternalEventQueue = mock()
        mockNewLineGenerator = mock()
        mockKale = MockKale()

        mockSink = mock()
        mockLineSource = mock()

        runner = IrcRunner(mockEventDispatcher, mockInternalEventQueue, mockNewLineGenerator, mockKale, mockSink, initialState)

        MockitoAnnotations.initMocks(this)
    }

    @Test fun test_run_RegistersHandlers() {
        whenever(mockSink.setUp()).thenReturn(true)

        runner.run()

        assertEquals(26, mockKale.spyRegisterHandlers.size)
        assertTrue(arrayContainsHandlerOfType<AuthenticateHandler>(mockKale.spyRegisterHandlers))
        assertTrue(arrayContainsHandlerOfType<Rpl903Handler>(mockKale.spyRegisterHandlers))
        assertTrue(arrayContainsHandlerOfType<Rpl904Handler>(mockKale.spyRegisterHandlers))
        assertTrue(arrayContainsHandlerOfType<Rpl905Handler>(mockKale.spyRegisterHandlers))
        assertTrue(arrayContainsHandlerOfType<CapLsHandler>(mockKale.spyRegisterHandlers))
        assertTrue(arrayContainsHandlerOfType<CapAckHandler>(mockKale.spyRegisterHandlers))
        assertTrue(arrayContainsHandlerOfType<CapNakHandler>(mockKale.spyRegisterHandlers))
        assertTrue(arrayContainsHandlerOfType<JoinHandler>(mockKale.spyRegisterHandlers))
        assertTrue(arrayContainsHandlerOfType<KickHandler>(mockKale.spyRegisterHandlers))
        assertTrue(arrayContainsHandlerOfType<ModeHandler>(mockKale.spyRegisterHandlers))
        assertTrue(arrayContainsHandlerOfType<NickHandler>(mockKale.spyRegisterHandlers))
        assertTrue(arrayContainsHandlerOfType<NoticeHandler>(mockKale.spyRegisterHandlers))
        assertTrue(arrayContainsHandlerOfType<PartHandler>(mockKale.spyRegisterHandlers))
        assertTrue(arrayContainsHandlerOfType<PingHandler>(mockKale.spyRegisterHandlers))
        assertTrue(arrayContainsHandlerOfType<PongHandler>(mockKale.spyRegisterHandlers))
        assertTrue(arrayContainsHandlerOfType<PrivMsgHandler>(mockKale.spyRegisterHandlers))
        assertTrue(arrayContainsHandlerOfType<QuitHandler>(mockKale.spyRegisterHandlers))
        assertTrue(arrayContainsHandlerOfType<TopicHandler>(mockKale.spyRegisterHandlers))
        assertTrue(arrayContainsHandlerOfType<Rpl005Handler>(mockKale.spyRegisterHandlers))
        assertTrue(arrayContainsHandlerOfType<Rpl332Handler>(mockKale.spyRegisterHandlers))
        assertTrue(arrayContainsHandlerOfType<Rpl353Handler>(mockKale.spyRegisterHandlers))
        assertTrue(arrayContainsHandlerOfType<Rpl376Handler>(mockKale.spyRegisterHandlers))
        assertTrue(arrayContainsHandlerOfType<Rpl471Handler>(mockKale.spyRegisterHandlers))
        assertTrue(arrayContainsHandlerOfType<Rpl473Handler>(mockKale.spyRegisterHandlers))
        assertTrue(arrayContainsHandlerOfType<Rpl474Handler>(mockKale.spyRegisterHandlers))
        assertTrue(arrayContainsHandlerOfType<Rpl475Handler>(mockKale.spyRegisterHandlers))
    }

    private inline fun <reified T: IKaleHandler<*>> arrayContainsHandlerOfType(iterable: Iterable<*>): Boolean {
        return iterable.any {
            it?.javaClass?.isAssignableFrom(T::class.java) ?: false
        }
    }

    @Test fun test_run_SendsRegistrationMessages() {
        whenever(mockSink.setUp()).thenReturn(true)

        runner.run()

        val inOrder = inOrder(mockSink)
        inOrder.verify(mockSink).write(NickMessage(nickname = connectionState.nickname))
        inOrder.verify(mockSink).write(UserMessage(username = connectionState.nickname, mode = "8", realname = connectionState.nickname))
    }

    @Test fun test_modeTakesAParameter_TypeDAlwaysFalse() {
        channelModesState.typeD = setOf('x')

        assertFalse(runner.modeTakesAParameter(isAdding = true, token = 'x'))
        assertFalse(runner.modeTakesAParameter(isAdding = false, token = 'x'))
    }

    @Test fun test_modeTakesAParameter_TypeABAlwaysTrue() {
        channelModesState.typeA = setOf('x')
        channelModesState.typeB = setOf('y')

        assertTrue(runner.modeTakesAParameter(isAdding = true, token = 'x'))
        assertTrue(runner.modeTakesAParameter(isAdding = false, token = 'x'))
        assertTrue(runner.modeTakesAParameter(isAdding = true, token = 'y'))
        assertTrue(runner.modeTakesAParameter(isAdding = false, token = 'y'))
    }

    @Test fun test_modeTakesAParameter_TypeCTrueIfAdding() {
        channelModesState.typeC = setOf('c')

        assertTrue(runner.modeTakesAParameter(isAdding = true, token = 'c'))
        assertFalse(runner.modeTakesAParameter(isAdding = false, token = 'c'))
    }

    @Test fun test_modeTakesAParameter_PrefixRelated_ReturnsTrue() {
        userPrefixesState.prefixesToModes = mapOf('+' to 'v')

        assertTrue(runner.modeTakesAParameter(isAdding = true, token = 'v'))
        assertTrue(runner.modeTakesAParameter(isAdding = false, token = 'v'))
    }

    @Test fun test_modeTakesAParameter_Unknown_NonPrefix_ReturnsFalse() {
        assertFalse(runner.modeTakesAParameter(isAdding = true, token = 'z'))
        assertFalse(runner.modeTakesAParameter(isAdding = false, token = 'z'))
    }

}

class MockKale : IKale {
    var spyRegisterHandlers = mutableListOf<IKaleHandler<*>>()

    override fun <T : IMessage> register(handler: IKaleHandler<T>) {
        spyRegisterHandlers.add(handler)
    }

    override fun serialise(message: Any): IrcMessage? {
        throw UnsupportedOperationException()
    }

    override fun process(line: String) {
        throw UnsupportedOperationException()
    }

    override var parsingStateDelegate: IKaleParsingStateDelegate? = null
}