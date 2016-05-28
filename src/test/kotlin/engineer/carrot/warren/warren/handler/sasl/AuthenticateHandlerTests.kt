package engineer.carrot.warren.warren.handler.sasl

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import engineer.carrot.warren.kale.irc.message.IMessage
import engineer.carrot.warren.kale.irc.message.ircv3.sasl.AuthenticateMessage
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.state.SaslCredentials
import engineer.carrot.warren.warren.state.SaslLifecycle
import engineer.carrot.warren.warren.state.SaslState
import org.junit.Before
import org.junit.Test

class AuthenticateHandlerTests {

    lateinit var handler: AuthenticateHandler
    lateinit var state: SaslState
    lateinit var sink: IMessageSink

    @Before fun setUp() {
        state = SaslState(shouldAuth = true, lifecycle = SaslLifecycle.AUTHING, credentials = null)
        sink = mock()

        handler = AuthenticateHandler(state, sink)
    }

    @Test fun test_handle_NotAuthing_DoesNothing() {
        state.lifecycle = SaslLifecycle.NO_AUTH

        handler.handle(AuthenticateMessage(payload = "+", isEmpty = true), mapOf())

        verify(sink, never()).write(any<IMessage>())
    }

    @Test fun test_handle_Authing_NoCredentials_DoesNothing() {
        state.lifecycle = SaslLifecycle.NO_AUTH
        state.credentials = null

        handler.handle(AuthenticateMessage(payload = "+", isEmpty = true), mapOf())

        verify(sink, never()).write(any<IMessage>())
    }

    @Test fun test_handle_Authing_WithCredentials_SendsSASLResponse() {
        state.lifecycle = SaslLifecycle.AUTHING
        state.credentials = SaslCredentials(account = "test-nick", password = "test-password")

        handler.handle(AuthenticateMessage(payload = "+", isEmpty = true), mapOf())

        verify(sink).write(AuthenticateMessage(payload = "dGVzdC1uaWNrAHRlc3QtbmljawB0ZXN0LXBhc3N3b3Jk", isEmpty = false))
    }

}