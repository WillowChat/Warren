package chat.willow.warren.extension.cap

import chat.willow.kale.IKaleIrcMessageHandler
import chat.willow.kale.IKaleRouter
import chat.willow.kale.helper.CaseMapping
import chat.willow.kale.irc.message.extension.cap.CapMessage
import chat.willow.warren.IMessageSink
import chat.willow.warren.event.IWarrenEventDispatcher
import chat.willow.warren.extension.monitor.MonitorState
import chat.willow.warren.extension.sasl.SaslState
import chat.willow.warren.registration.IRegistrationManager
import chat.willow.warren.state.AuthLifecycle
import chat.willow.warren.state.CaseMappingState
import chat.willow.warren.state.emptyChannelsState
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.never

class CapManagerTests {

    private lateinit var sut: CapManager
    private lateinit var mockKaleRouter: IKaleRouter<IKaleIrcMessageHandler>
    private lateinit var mockSink: IMessageSink
    private lateinit var mockRegistrationManager: IRegistrationManager
    private lateinit var mockEventDispatcher: IWarrenEventDispatcher

    private var initialState = CapState(lifecycle = CapLifecycle.NEGOTIATING, negotiate = setOf(), server = mapOf(), accepted = setOf(), rejected = setOf())
    private var caseMappingState = CaseMappingState(CaseMapping.RFC1459)
    private var channelsState = emptyChannelsState(caseMappingState)
    private var initialSaslState = SaslState(shouldAuth = true, lifecycle = AuthLifecycle.AUTHING, credentials = null)
    private var initialMonitorState = MonitorState(maxCount = 0)

    @Before fun setUp() {
        mockKaleRouter = mock()
        mockSink = mock()
        mockRegistrationManager = mock()
        mockEventDispatcher = mock()

        sut = CapManager(initialState, mockKaleRouter, channelsState, initialSaslState, initialMonitorState, mockSink, caseMappingState, mockRegistrationManager, mockEventDispatcher)
    }

    @Test fun test_onRegistrationStateChanged_NotNegotiatingCaps_NoSasl_SendsCapEnd() {
        initialState.lifecycle = CapLifecycle.NEGOTIATED
        initialState.negotiate = setOf("cap1", "cap2")
        initialState.accepted = setOf("cap1")
        initialState.rejected = setOf("cap2")

        sut = CapManager(initialState, mockKaleRouter, channelsState, initialSaslState, initialMonitorState, mockSink, caseMappingState, mockRegistrationManager, mockEventDispatcher)

        sut.onRegistrationStateChanged()

        verify(mockSink).write(CapMessage.End.Command)
    }

    @Test fun test_onRegistrationStateChanged_NegotiatingCaps_NoSasl_DoesNotSendCapEnd() {
        initialState.lifecycle = CapLifecycle.NEGOTIATING
        initialState.negotiate = setOf("cap1", "cap2", "not-negotiated")
        initialState.accepted = setOf("cap1")
        initialState.rejected = setOf("cap2")

        sut = CapManager(initialState, mockKaleRouter, channelsState, initialSaslState, initialMonitorState, mockSink, caseMappingState, mockRegistrationManager, mockEventDispatcher)

        sut.onRegistrationStateChanged()

        verify(mockSink, never()).write(CapMessage.End.Command)
    }

    @Test fun test_onRegistrationStateChanged_NotNegotatingCaps_WaitingForSasl_DoesNotSendCapEnd() {
        initialState.lifecycle = CapLifecycle.NEGOTIATING
        initialState.negotiate = setOf("cap1", "cap2", "sasl")
        initialState.accepted = setOf("cap1", "sasl")
        initialState.rejected = setOf("cap2")

        sut = CapManager(initialState, mockKaleRouter, channelsState, initialSaslState, initialMonitorState, mockSink, caseMappingState, mockRegistrationManager, mockEventDispatcher)

        sut.onRegistrationStateChanged()

        verify(mockSink, never()).write(CapMessage.End.Command)
    }

    @Test fun test_startRegistration_WritesCapLs() {
        sut.startRegistration()

        verify(mockSink).write(CapMessage.Ls.Command(version = "302"))
    }

    @Test fun test_onRegistrationSucceeded_TellsRegistrationManager_Success() {
        sut.onRegistrationSucceeded()

        verify(mockRegistrationManager).onExtensionSuccess(sut)
    }

    @Test fun test_onRegistrationFailed_TellsRegistrationManager_Failure() {
        sut.onRegistrationFailed()

        verify(mockRegistrationManager).onExtensionFailure(sut)
    }

    // TODO: capEnabled, capDisabled and capValueSet

}