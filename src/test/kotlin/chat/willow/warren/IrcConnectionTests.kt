package chat.willow.warren

import chat.willow.kale.IKale
import chat.willow.kale.IKaleIrcMessageHandler
import chat.willow.kale.IKaleRouter
import chat.willow.kale.KaleSubcommandHandler
import chat.willow.kale.helper.CaseMapping
import chat.willow.kale.irc.message.rfc1459.JoinMessage
import chat.willow.kale.irc.message.rfc1459.PingMessage
import chat.willow.warren.event.ConnectionLifecycleEvent
import chat.willow.warren.event.IWarrenEventDispatcher
import chat.willow.warren.event.internal.IWarrenInternalEventGenerator
import chat.willow.warren.event.internal.IWarrenInternalEventQueue
import chat.willow.warren.extension.cap.CapLifecycle
import chat.willow.warren.extension.cap.CapState
import chat.willow.warren.extension.monitor.MonitorState
import chat.willow.warren.extension.sasl.SaslState
import chat.willow.warren.handler.*
import chat.willow.warren.handler.rpl.*
import chat.willow.warren.handler.rpl.isupport.Rpl005Handler
import chat.willow.warren.helper.IExecutionContext
import chat.willow.warren.helper.ISleeper
import chat.willow.warren.helper.SimpleBlock
import chat.willow.warren.registration.IRegistrationManager
import chat.willow.warren.state.*
import com.nhaarman.mockito_kotlin.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.never

class IrcRunnerTests {

    lateinit var connection: IrcConnection
    lateinit var connectionState: ConnectionState
    lateinit var channelModesState: ChannelModesState
    lateinit var userPrefixesState: UserPrefixesState
    lateinit var channelsState: ChannelsState
    lateinit var monitorState: MonitorState

    lateinit var mockEventDispatcher: IWarrenEventDispatcher
    lateinit var mockInternalEventQueue: IWarrenInternalEventQueue
    lateinit var mockNewLineGenerator: IWarrenInternalEventGenerator
    lateinit var mockKale: IKale
    lateinit var mockKaleRouter: IKaleRouter<IKaleIrcMessageHandler>
    lateinit var mockSink: IMessageSink
    lateinit var mockLineSource: ILineSource
    lateinit var mockRegistrationManager: IRegistrationManager
    lateinit var mockSleeper: ISleeper
    lateinit var mockPingExecutionContext: IExecutionContext
    lateinit var mockLineExecutionContext: IExecutionContext

    @Before fun setUp() {
        val lifecycleState = LifecycleState.DISCONNECTED
        val capLifecycleState = CapLifecycle.NEGOTIATED
        val capState = CapState(lifecycle = capLifecycleState, negotiate = setOf(), server = mapOf(), accepted = setOf(), rejected = setOf())
        connectionState = ConnectionState(server = "test.server", port = 6697, nickname = "test-nick", user = "test-nick", lifecycle = lifecycleState)

        userPrefixesState = UserPrefixesState(prefixesToModes = mapOf('@' to 'o', '+' to 'v'))
        channelModesState = ChannelModesState(typeA = setOf('e', 'I', 'b'), typeB = setOf('k'), typeC = setOf('l'), typeD = setOf('i', 'm', 'n', 'p', 's', 't', 'S', 'r'))

        val channelPrefixesState = ChannelTypesState(types = setOf('#', '&'))
        val caseMappingState = CaseMappingState(mapping = CaseMapping.RFC1459)
        val parsingState = ParsingState(userPrefixesState, channelModesState, channelPrefixesState, caseMappingState)

        channelsState = ChannelsState(joining = JoiningChannelsState(caseMappingState), joined = JoinedChannelsState(caseMappingState))
        monitorState = MonitorState(maxCount = 0)

        val initialState = IrcState(connectionState, parsingState, channelsState)

        mockEventDispatcher = mock()
        mockInternalEventQueue = mock()
        mockNewLineGenerator = mock()
        mockKale = mock()
        mockKaleRouter = mock()

        mockSink = mock()
        mockLineSource = mock()

        mockRegistrationManager = mock()
        mockSleeper = mock()

        mockPingExecutionContext = mock()
        mockLineExecutionContext = mock()

        val saslState = SaslState(shouldAuth = false, lifecycle = AuthLifecycle.NO_AUTH, credentials = null)

        connection = IrcConnection(mockEventDispatcher, mockInternalEventQueue, mockNewLineGenerator, mockKale, mockKaleRouter, mockSink, initialState, initialCapState = capState, initialSaslState = saslState, initialMonitorState = monitorState, registrationManager = mockRegistrationManager, sleeper = mockSleeper, pingGeneratorExecutionContext = mockPingExecutionContext, lineGeneratorExecutionContext = mockLineExecutionContext)
    }

    @Test fun test_run_RegistersBaseHandlers() {
        whenever(mockSink.setUp()).thenReturn(true)

        connection.start()

        verify(mockKaleRouter, times(20)).register(any(), any())

        verify(mockKaleRouter).register(eq("CAP"), isA<KaleSubcommandHandler>())
        verify(mockKaleRouter).register(eq("JOIN"), isA<JoinHandler>())
        verify(mockKaleRouter).register(eq("KICK"), isA<KickHandler>())
        verify(mockKaleRouter).register(eq("MODE"), isA<ModeHandler>())
        verify(mockKaleRouter).register(eq("NICK"), isA<NickHandler>())
        verify(mockKaleRouter).register(eq("NOTICE"), isA<NoticeHandler>())
        verify(mockKaleRouter).register(eq("PART"), isA<PartHandler>())
        verify(mockKaleRouter).register(eq("PING"), isA<PingHandler>())
        verify(mockKaleRouter).register(eq("PONG"), isA<PongHandler>())
        verify(mockKaleRouter).register(eq("PRIVMSG"), isA<PrivMsgHandler>())
        verify(mockKaleRouter).register(eq("QUIT"), isA<QuitHandler>())
        verify(mockKaleRouter).register(eq("TOPIC"), isA<TopicHandler>())

        verify(mockKaleRouter).register(eq("005"), isA<Rpl005Handler>())
        verify(mockKaleRouter).register(eq("332"), isA<Rpl332Handler>())
        verify(mockKaleRouter).register(eq("353"), isA<Rpl353Handler>())
        verify(mockKaleRouter).register(eq("376"), isA<Rpl376Handler>())
        verify(mockKaleRouter).register(eq("471"), isA<Rpl471Handler>())
        verify(mockKaleRouter).register(eq("473"), isA<Rpl473Handler>())
        verify(mockKaleRouter).register(eq("474"), isA<Rpl474Handler>())
        verify(mockKaleRouter).register(eq("475"), isA<Rpl475Handler>())
    }

    @Test fun test_run_startsRegistration() {
        whenever(mockSink.setUp()).thenReturn(true)

        connection.start()

        verify(mockRegistrationManager).startRegistration()
    }

    @Test fun test_run_UsesPingExecutionContext() {
        whenever(mockSink.setUp()).thenReturn(true)

        connection.start()

        verify(mockPingExecutionContext, times(1)).execute(any())
    }

    @Test fun test_run_UsesLineExecutionContext() {
        whenever(mockSink.setUp()).thenReturn(true)

        connection.start()

        verify(mockLineExecutionContext, times(1)).execute(any())
    }

    @Test fun test_run_TearsDownSink() {
        whenever(mockSink.setUp()).thenReturn(true)

        connection.start()

        verify(mockSink).tearDown()
    }

    @Test fun test_run_TearsDownPingExecutionContext() {
        whenever(mockSink.setUp()).thenReturn(true)

        connection.start()

        verify(mockPingExecutionContext).tearDown()
    }

    @Test fun test_run_TearsDownLineExecutionContext() {
        whenever(mockSink.setUp()).thenReturn(true)

        connection.start()

        verify(mockLineExecutionContext).tearDown()
    }

    @Test fun test_run_PingBlock_SleepsForTenSecondsFirst() {
        whenever(mockSink.setUp()).thenReturn(true)

        connection.start()

        whenever(mockSleeper.sleep(any()))
                .thenReturn(true)
                .thenReturn(false)

        captureAndInvokeBlockFromContext(mockPingExecutionContext)

        inOrder(mockSleeper, mockInternalEventQueue) {
            verify(mockSleeper).sleep(10 * 1000)
            verify(mockInternalEventQueue).add(any<SimpleBlock>())
            verify(mockSleeper).sleep(10 * 1000)
        }
    }

    private fun captureAndInvokeBlockFromContext(mockContext: IExecutionContext) {
        val blockCaptor = argumentCaptor<SimpleBlock>()
        verify(mockContext, times(1)).execute(blockCaptor.capture())
        val pingBlock = blockCaptor.firstValue

        pingBlock.invoke()
    }

    @Test fun test_run_PingBlock_LifecycleNotConnected_DoesNotWriteAnything() {
        whenever(mockSink.setUp()).thenReturn(true)

        connection.start()

        whenever(mockSleeper.sleep(any()))
                .thenReturn(true)
                .thenReturn(false)

        captureAndInvokeBlockFromContext(mockPingExecutionContext)
        captureAndInvokeBlockFromEventQueue()

        verify(mockSink, never()).write(any<PingMessage>())
    }

    private fun captureAndInvokeBlockFromEventQueue() {
        val blockCaptor = argumentCaptor<SimpleBlock>()
        verify(mockInternalEventQueue, times(1)).add(blockCaptor.capture())
        val pingBlock = blockCaptor.firstValue

        pingBlock.invoke()
    }

    @Test fun test_run_PingBlock_LifecycleConnected_ReadyForPing_SendsPing() {
        whenever(mockSink.setUp()).thenReturn(true)

        connection.start()

        // FIXME: Relies on internal implementation too much
        whenever(mockSleeper.sleep(any())).then {
            connection.onRegistrationEnded()

            true
        }.thenReturn(false)

        captureAndInvokeBlockFromContext(mockPingExecutionContext)
        captureAndInvokeBlockFromEventQueue()

        verify(mockSink).write(any<PingMessage.Command>())
    }

    @Test fun test_run_LineBlock_TellsNewLineGeneratorToRun() {
        whenever(mockSink.setUp()).thenReturn(true)

        connection.start()

        captureAndInvokeBlockFromContext(mockLineExecutionContext)

        verify(mockNewLineGenerator).run()
    }

    @Test fun test_run_LineBlock_NewLineGeneratorEnds_ClearsEventQueue() {
        whenever(mockSink.setUp()).thenReturn(true)
        whenever(mockNewLineGenerator.run()).then { /* NO-OP */ }

        connection.start()

        captureAndInvokeBlockFromContext(mockLineExecutionContext)

        verify(mockInternalEventQueue).clear()
    }

    @Test fun test_run_LineBlock_NewLineGeneratorEnds_AddsEventToMakeLifecycledDisconnected() {
        whenever(mockSink.setUp()).thenReturn(true)
        whenever(mockNewLineGenerator.run()).then { /* NO-OP */ }

        connection.start()

        captureAndInvokeBlockFromContext(mockLineExecutionContext)
        captureAndInvokeBlockFromEventQueue()

        assertEquals(LifecycleState.DISCONNECTED, connection.state.connection.lifecycle)
    }

    @Test fun test_modeTakesAParameter_TypeDAlwaysFalse() {
        channelModesState.typeD = setOf('x')

        assertFalse(connection.modeTakesAParameter(isAdding = true, token = 'x'))
        assertFalse(connection.modeTakesAParameter(isAdding = false, token = 'x'))
    }

    @Test fun test_modeTakesAParameter_TypeABAlwaysTrue() {
        channelModesState.typeA = setOf('x')
        channelModesState.typeB = setOf('y')

        assertTrue(connection.modeTakesAParameter(isAdding = true, token = 'x'))
        assertTrue(connection.modeTakesAParameter(isAdding = false, token = 'x'))
        assertTrue(connection.modeTakesAParameter(isAdding = true, token = 'y'))
        assertTrue(connection.modeTakesAParameter(isAdding = false, token = 'y'))
    }

    @Test fun test_modeTakesAParameter_TypeCTrueIfAdding() {
        channelModesState.typeC = setOf('c')

        assertTrue(connection.modeTakesAParameter(isAdding = true, token = 'c'))
        assertFalse(connection.modeTakesAParameter(isAdding = false, token = 'c'))
    }

    @Test fun test_modeTakesAParameter_PrefixRelated_ReturnsTrue() {
        userPrefixesState.prefixesToModes = mapOf('+' to 'v')

        assertTrue(connection.modeTakesAParameter(isAdding = true, token = 'v'))
        assertTrue(connection.modeTakesAParameter(isAdding = false, token = 'v'))
    }

    @Test fun test_modeTakesAParameter_Unknown_NonPrefix_ReturnsFalse() {
        assertFalse(connection.modeTakesAParameter(isAdding = true, token = 'z'))
        assertFalse(connection.modeTakesAParameter(isAdding = false, token = 'z'))
    }

    // IRegistrationListener

    @Test fun test_onRegistrationEnded_Connecting_NickservAuthOn_WithCredentials_SendsIdentifyMessage() {
        connectionState.lifecycle = LifecycleState.CONNECTING
        connectionState.nickServ.shouldAuth = true
        connectionState.nickServ.credentials = AuthCredentials(account = "test-user", password = "test-password")

        connection.onRegistrationEnded()

        verify(mockSink).writeRaw("NICKSERV identify test-user test-password")
    }

    @Test fun test_onRegistrationEnded_Registering_NickservAuthOn_WithCredentials_SendsIdentifyMessage() {
        connectionState.lifecycle = LifecycleState.REGISTERING
        connectionState.nickServ.shouldAuth = true
        connectionState.nickServ.credentials = AuthCredentials(account = "test-user", password = "test-password")

        connection.onRegistrationEnded()

        verify(mockSink).writeRaw("NICKSERV identify test-user test-password")
    }

    @Test fun test_onRegistrationEnded_AllSuccessful_WaitsBeforeJoiningChannels() {
        connectionState.lifecycle = LifecycleState.REGISTERING
        connectionState.nickServ.shouldAuth = true
        connectionState.nickServ.credentials = AuthCredentials(account = "test-user", password = "test-password")

        connection.onRegistrationEnded()

        verify(mockSleeper).sleep(connectionState.nickServ.channelJoinWaitSeconds * 1000L)
    }

    @Test fun test_onRegistrationEnded_NickservAuthOn_NoCredentials_DoesNotWriteAnything() {
        connectionState.lifecycle = LifecycleState.REGISTERING
        connectionState.nickServ.shouldAuth = true
        connectionState.nickServ.credentials = null

        connection.onRegistrationEnded()

        verify(mockSink, never()).writeRaw(any())
    }

    @Test fun test_onRegistrationEnded_AtLeastOneChannel_JoinsChannels() {
        connectionState.lifecycle = LifecycleState.REGISTERING
        channelsState.joining += JoiningChannelState("#test", status = JoiningChannelLifecycle.JOINING)
        channelsState.joining += JoiningChannelState("#test2", key = "testpass", status = JoiningChannelLifecycle.JOINING)

        connection.onRegistrationEnded()

        inOrder(mockSink) {
            verify(mockSink).write(JoinMessage.Command(channels = listOf("#test")))
            verify(mockSink).write(JoinMessage.Command(channels = listOf("#test2"), keys = listOf("testpass")))
        }
    }

    @Test fun test_onRegistrationEnded_NoChannelsToJoin_DoesNotWriteJoinMessages() {
        connectionState.lifecycle = LifecycleState.REGISTERING
        channelsState.joining.clear()

        connection.onRegistrationEnded()

        verify(mockSink, never()).write(any<JoinMessage>())
    }

    @Test fun test_onRegistrationEnded_SetsConnectionLifecycleToConnected() {
        connection.onRegistrationEnded()

        assertEquals(LifecycleState.CONNECTED, connectionState.lifecycle)
    }

    @Test fun test_onRegistrationEnded_FiresConnectionLifecycleEvent_WithConnected() {
        connection.onRegistrationEnded()

        verify(mockEventDispatcher).fire(ConnectionLifecycleEvent(LifecycleState.CONNECTED))
    }

}