package chat.willow.warren.extension.sasl.handler

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import chat.willow.kale.irc.message.IMessage
import chat.willow.kale.irc.message.extension.sasl.AuthenticateMessage
import chat.willow.warren.IMessageSink
import chat.willow.warren.extension.sasl.AuthenticateHandler
import chat.willow.warren.extension.sasl.SaslState
import chat.willow.warren.state.AuthCredentials
import chat.willow.warren.state.AuthLifecycle
import org.junit.Before
import org.junit.Test

class AuthenticateHandlerTests {

    lateinit var handler: AuthenticateHandler
    lateinit var state: SaslState
    lateinit var sink: IMessageSink

    @Before fun setUp() {
        state = SaslState(shouldAuth = true, lifecycle = AuthLifecycle.AUTHING, credentials = null)
        sink = mock()

        handler = AuthenticateHandler(state, sink)
    }

    @Test fun test_handle_NotAuthing_DoesNothing() {
        state.lifecycle = AuthLifecycle.NO_AUTH

        handler.handle(AuthenticateMessage(payload = "+", isEmpty = true), mapOf())

        verify(sink, never()).write(any<IMessage>())
    }

    @Test fun test_handle_Authing_NoCredentials_DoesNothing() {
        state.lifecycle = AuthLifecycle.NO_AUTH
        state.credentials = null

        handler.handle(AuthenticateMessage(payload = "+", isEmpty = true), mapOf())

        verify(sink, never()).write(any<IMessage>())
    }

    @Test fun test_handle_Authing_WithCredentials_SendsSASLResponse() {
        state.lifecycle = AuthLifecycle.AUTHING
        state.credentials = AuthCredentials(account = "test-nick", password = "test-password")

        handler.handle(AuthenticateMessage(payload = "+", isEmpty = true), mapOf())

        verify(sink).write(AuthenticateMessage(payload = "dGVzdC1uaWNrAHRlc3QtbmljawB0ZXN0LXBhc3N3b3Jk", isEmpty = false))
    }

}