package engineer.carrot.warren.warren

import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import engineer.carrot.warren.kale.IKale
import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.IKaleParsingStateDelegate
import engineer.carrot.warren.kale.irc.message.IMessage
import engineer.carrot.warren.kale.irc.message.IrcMessage
import engineer.carrot.warren.kale.irc.message.rfc1459.NickMessage
import engineer.carrot.warren.kale.irc.message.rfc1459.UserMessage
import engineer.carrot.warren.kale.irc.message.utility.CaseMapping
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
    lateinit var mockKale: MockKale
    lateinit var mockSink: IMessageSink
    lateinit var mockLineSource: ILineSource

    @Before fun setUp() {
        val lifecycleState = LifecycleState.DISCONNECTED
        val capLifecycleState = CapLifecycle.NEGOTIATED
        val capState = CapState(lifecycle = capLifecycleState, negotiate = setOf(), server = mapOf(), accepted = setOf(), rejected = setOf())
        val saslState = SaslState(shouldAuth = false, lifecycle = SaslLifecycle.AUTH_FAILED, credentials = null)
        connectionState = ConnectionState(server = "test.server", port = 6697, nickname = "test-nick", username = "test-nick", lifecycle = lifecycleState, cap = capState, sasl = saslState)

        userPrefixesState = UserPrefixesState(prefixesToModes = mapOf('@' to 'o', '+' to 'v'))
        channelModesState = ChannelModesState(typeA = setOf('e', 'I', 'b'), typeB = setOf('k'), typeC = setOf('l'), typeD = setOf('i', 'm', 'n', 'p', 's', 't', 'S', 'r'))
        val channelPrefixesState = ChannelTypesState(types = setOf('#', '&'))
        val caseMappingState = CaseMappingState(mapping = CaseMapping.RFC1459)
        val parsingState = ParsingState(userPrefixesState, channelModesState, channelPrefixesState, caseMappingState)
        val channelsState = ChannelsState(joining = JoiningChannelsState(caseMappingState), joined = JoinedChannelsState(caseMappingState))

        val initialState = IrcState(connectionState, parsingState, channelsState)

        mockEventDispatcher = mock()
        mockKale = MockKale()

        mockSink = mock()
        mockLineSource = mock()

        runner = IrcRunner(mockEventDispatcher, mockKale, mockSink, mockLineSource, initialState, false)

        MockitoAnnotations.initMocks(this)
    }

    @Test fun test_run_RegistersHandlers() {
        runner.run()

        assertEquals(26, mockKale.spyRegisterHandlers.size)
        assertTrue(mockKale.spyRegisterHandlers[0] is AuthenticateHandler)
        assertTrue(mockKale.spyRegisterHandlers[1] is Rpl903Handler)
        assertTrue(mockKale.spyRegisterHandlers[2] is Rpl904Handler)
        assertTrue(mockKale.spyRegisterHandlers[3] is Rpl905Handler)
        assertTrue(mockKale.spyRegisterHandlers[4] is CapLsHandler)
        assertTrue(mockKale.spyRegisterHandlers[5] is CapAckHandler)
        assertTrue(mockKale.spyRegisterHandlers[6] is CapNakHandler)
        assertTrue(mockKale.spyRegisterHandlers[7] is JoinHandler)
        assertTrue(mockKale.spyRegisterHandlers[8] is KickHandler)
        assertTrue(mockKale.spyRegisterHandlers[9] is ModeHandler)
        assertTrue(mockKale.spyRegisterHandlers[10] is NickHandler)
        assertTrue(mockKale.spyRegisterHandlers[11] is NoticeHandler)
        assertTrue(mockKale.spyRegisterHandlers[12] is PartHandler)
        assertTrue(mockKale.spyRegisterHandlers[13] is PingHandler)
        assertTrue(mockKale.spyRegisterHandlers[14] is PongHandler)
        assertTrue(mockKale.spyRegisterHandlers[15] is PrivMsgHandler)
        assertTrue(mockKale.spyRegisterHandlers[16] is QuitHandler)
        assertTrue(mockKale.spyRegisterHandlers[17] is TopicHandler)
        assertTrue(mockKale.spyRegisterHandlers[18] is Rpl005Handler)
        assertTrue(mockKale.spyRegisterHandlers[19] is Rpl332Handler)
        assertTrue(mockKale.spyRegisterHandlers[20] is Rpl353Handler)
        assertTrue(mockKale.spyRegisterHandlers[21] is Rpl376Handler)
        assertTrue(mockKale.spyRegisterHandlers[22] is Rpl471Handler)
        assertTrue(mockKale.spyRegisterHandlers[23] is Rpl473Handler)
        assertTrue(mockKale.spyRegisterHandlers[24] is Rpl474Handler)
        assertTrue(mockKale.spyRegisterHandlers[25] is Rpl475Handler)
    }

    @Test fun test_run_SendsRegistrationMessages() {
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