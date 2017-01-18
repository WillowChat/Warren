package chat.willow.warren.handler.rpl

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import engineer.carrot.warren.kale.irc.message.rfc1459.JoinMessage
import engineer.carrot.warren.kale.irc.message.rfc1459.rpl.Rpl376Message
import chat.willow.warren.IMessageSink
import chat.willow.warren.event.ConnectionLifecycleEvent
import chat.willow.warren.event.IWarrenEventDispatcher
import chat.willow.warren.extension.cap.CapLifecycle
import chat.willow.warren.extension.cap.CapState
import chat.willow.warren.registration.IRegistrationExtension
import chat.willow.warren.state.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class Rpl376HandlerTests {
    lateinit var handler: Rpl376Handler
    lateinit var mockSink: IMessageSink
    lateinit var connectionState: ConnectionState
    lateinit var capState: CapState
    lateinit var mockEventDispatcher: IWarrenEventDispatcher
    lateinit var mockRFC1459RegistrationExtension: IRegistrationExtension
    lateinit var mockCapRegistrationExtension: IRegistrationExtension

    @Before fun setUp() {
        val lifecycleState = LifecycleState.CONNECTING
        val capLifecycleState = CapLifecycle.NEGOTIATED
        capState = CapState(lifecycle = capLifecycleState, negotiate = setOf(), server = mapOf(), accepted = setOf(), rejected = setOf())
        connectionState = ConnectionState(server = "test.server", port = 6697, nickname = "test-nick", user = "test-nick", lifecycle = lifecycleState)

        mockSink = mock()
        mockEventDispatcher = mock()
        mockRFC1459RegistrationExtension = mock()
        mockCapRegistrationExtension = mock()

        handler = Rpl376Handler(mockSink, capState, mockRFC1459RegistrationExtension, mockCapRegistrationExtension)
    }

    @Test fun test_handle_TellsRFC1459RegistrationExtension_Succeeded() {
        handler.handle(Rpl376Message(source = "test.source", target = "test-user", contents = "end of motd"), mapOf())

        verify(mockRFC1459RegistrationExtension).onRegistrationSucceeded()
    }

    @Test fun test_handle_CapLifecycleIsNegotiating_TellsCapRegistrationExtension_Failure() {
        capState.lifecycle = CapLifecycle.NEGOTIATING

        handler.handle(Rpl376Message(source = "test.source", target = "test-user", contents = "end of motd"), mapOf())

        verify(mockCapRegistrationExtension).onRegistrationFailed()
    }

    @Test fun test_handle_CapLifecycleIsNegotiated_DoesNotNotifyCapRegistrationExtension() {
        capState.lifecycle = CapLifecycle.NEGOTIATED

        handler.handle(Rpl376Message(source = "test.source", target = "test-user", contents = "end of motd"), mapOf())

        verify(mockCapRegistrationExtension, never()).onRegistrationFailed()
    }

}