package chat.willow.warren.extension.sasl.handler

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import chat.willow.kale.irc.message.IMessage
import chat.willow.kale.irc.message.extension.cap.CapEndMessage
import chat.willow.kale.irc.message.extension.sasl.Rpl903Message
import chat.willow.kale.irc.message.extension.sasl.Rpl904Message
import chat.willow.warren.IMessageSink
import chat.willow.warren.extension.cap.CapLifecycle
import chat.willow.warren.extension.cap.CapState
import chat.willow.warren.extension.cap.ICapManager
import chat.willow.warren.extension.sasl.Rpl904Handler
import chat.willow.warren.extension.sasl.SaslState
import chat.willow.warren.state.AuthLifecycle
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class Rpl904HandlerTests {

    private lateinit var handler: Rpl904Handler
    private lateinit var capState: CapState
    private lateinit var saslState: SaslState
    private lateinit var mockCapManager: ICapManager

    @Before fun setUp() {
        val capLifecycleState = CapLifecycle.NEGOTIATING
        capState = CapState(lifecycle = capLifecycleState, negotiate = setOf(), server = mapOf(), accepted = setOf(), rejected = setOf())
        saslState = SaslState(shouldAuth = false, lifecycle = AuthLifecycle.AUTHING, credentials = null)
        mockCapManager = mock()

        handler = Rpl904Handler(mockCapManager, saslState)
    }

    @Test fun test_handle_LifecycleSetToAuthFailed() {
        handler.handle(Rpl904Message(source = "", target = "", contents = "SASL auth failed"), mapOf())

        assertEquals(AuthLifecycle.AUTH_FAILED, saslState.lifecycle)
    }

    @Test fun test_handle_TellsCapManagerRegistrationStateChanged() {
        handler.handle(Rpl904Message(source = "", target = "", contents = "SASL auth failed"), mapOf())

        verify(mockCapManager).onRegistrationStateChanged()
    }

}