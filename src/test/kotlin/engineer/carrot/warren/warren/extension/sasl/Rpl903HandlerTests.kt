package engineer.carrot.warren.warren.extension.sasl

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import engineer.carrot.warren.kale.irc.message.IMessage
import engineer.carrot.warren.kale.irc.message.extension.cap.CapEndMessage
import engineer.carrot.warren.kale.irc.message.extension.sasl.Rpl903Message
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.extension.cap.CapLifecycle
import engineer.carrot.warren.warren.extension.cap.CapState
import engineer.carrot.warren.warren.extension.cap.ICapManager
import engineer.carrot.warren.warren.state.AuthLifecycle
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class Rpl903HandlerTests {

    private lateinit var handler: Rpl903Handler
    private lateinit var capState: CapState
    private lateinit var saslState: SaslState
    private lateinit var mockCapManager: ICapManager

    @Before fun setUp() {
        val capLifecycleState = CapLifecycle.NEGOTIATING
        capState = CapState(lifecycle = capLifecycleState, negotiate = setOf(), server = mapOf(), accepted = setOf(), rejected = setOf())
        saslState = SaslState(shouldAuth = false, lifecycle = AuthLifecycle.AUTHING, credentials = null)
        mockCapManager = mock()

        handler = Rpl903Handler(mockCapManager, saslState)
    }

    @Test fun test_handle_LifecycleSetToAuthed() {
        handler.handle(Rpl903Message(source = "", target = "", contents = "SASL auth succeeded"), mapOf())

        assertEquals(AuthLifecycle.AUTHED, saslState.lifecycle)
    }

    @Test fun test_handle_TellsCapManagerRegistrationStateChanged() {
        handler.handle(Rpl903Message(source = "", target = "", contents = "SASL auth succeeded"), mapOf())

        verify(mockCapManager).onRegistrationStateChanged()
    }

}