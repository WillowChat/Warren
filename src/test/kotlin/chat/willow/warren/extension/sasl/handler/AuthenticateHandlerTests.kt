package chat.willow.warren.extension.sasl.handler

import chat.willow.kale.irc.message.IMessage
import chat.willow.kale.irc.message.extension.sasl.AuthenticateMessage
import chat.willow.kale.irc.tag.TagStore
import chat.willow.warren.IMessageSink
import chat.willow.warren.extension.sasl.AuthenticateHandler
import chat.willow.warren.extension.sasl.SaslState
import chat.willow.warren.state.AuthCredentials
import chat.willow.warren.state.AuthLifecycle
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
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

        handler.handle(AuthenticateMessage.Message(payload = "+", isEmpty = true), TagStore())

        verify(sink, never()).write(any<IMessage>())
    }

    @Test fun test_handle_Authing_NoCredentials_DoesNothing() {
        state.lifecycle = AuthLifecycle.NO_AUTH
        state.credentials = null

        handler.handle(AuthenticateMessage.Message(payload = "+", isEmpty = true), TagStore())

        verify(sink, never()).write(any<IMessage>())
    }

    @Test fun test_handle_Authing_WithCredentials_SendsSASLResponse() {
        state.lifecycle = AuthLifecycle.AUTHING
        state.credentials = AuthCredentials(account = "test-nick", password = "test-password")

        handler.handle(AuthenticateMessage.Message(payload = "+", isEmpty = true), TagStore())

        verify(sink).write(AuthenticateMessage.Command(payload = "dGVzdC1uaWNrAHRlc3QtbmljawB0ZXN0LXBhc3N3b3Jk"))
    }

}