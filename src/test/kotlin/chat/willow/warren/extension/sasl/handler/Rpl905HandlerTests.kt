package chat.willow.warren.extension.sasl.handler

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import chat.willow.kale.irc.message.IMessage
import chat.willow.kale.irc.message.extension.cap.CapEndMessage
import chat.willow.kale.irc.message.extension.sasl.Rpl903Message
import chat.willow.kale.irc.message.extension.sasl.Rpl905Message
import chat.willow.kale.irc.tag.TagStore
import chat.willow.warren.IMessageSink
import chat.willow.warren.extension.cap.CapLifecycle
import chat.willow.warren.extension.cap.CapState
import chat.willow.warren.extension.cap.ICapManager
import chat.willow.warren.extension.sasl.Rpl905Handler
import chat.willow.warren.extension.sasl.SaslState
import chat.willow.warren.state.AuthLifecycle
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class Rpl905HandlerTests {

    lateinit var handler: Rpl905Handler
    lateinit var capState: CapState
    lateinit var saslState: SaslState
    lateinit var mockCapManager: ICapManager

    @Before fun setUp() {
        val capLifecycleState = CapLifecycle.NEGOTIATING
        capState = CapState(lifecycle = capLifecycleState, negotiate = setOf(), server = mapOf(), accepted = setOf(), rejected = setOf())
        saslState = SaslState(shouldAuth = false, lifecycle = AuthLifecycle.AUTHING, credentials = null)
        mockCapManager = mock()

        handler = Rpl905Handler(mockCapManager, saslState)
    }

    @Test fun test_handle_LifecycleSetToAuthFailed() {
        handler.handle(Rpl905Message(source = "", target = "", contents = "SASL auth failed"), TagStore())

        assertEquals(AuthLifecycle.AUTH_FAILED, saslState.lifecycle)
    }

    @Test fun test_handle_TellsCapManagerRegistrationStateChanged() {
        handler.handle(Rpl905Message(source = "", target = "", contents = "SASL auth failed"), TagStore())

        verify(mockCapManager).onRegistrationStateChanged()
    }

}