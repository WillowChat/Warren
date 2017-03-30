package chat.willow.warren.extension.sasl.handler

import chat.willow.kale.irc.message.extension.sasl.rpl.Rpl903MessageType
import chat.willow.kale.irc.tag.TagStore
import chat.willow.warren.extension.cap.CapLifecycle
import chat.willow.warren.extension.cap.CapState
import chat.willow.warren.extension.cap.ICapManager
import chat.willow.warren.extension.sasl.Rpl903Handler
import chat.willow.warren.extension.sasl.SaslState
import chat.willow.warren.state.AuthLifecycle
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
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
        handler.handle(Rpl903MessageType(source = "", target = "", contents = "SASL auth succeeded"), TagStore())

        assertEquals(AuthLifecycle.AUTHED, saslState.lifecycle)
    }

    @Test fun test_handle_TellsCapManagerRegistrationStateChanged() {
        handler.handle(Rpl903MessageType(source = "", target = "", contents = "SASL auth succeeded"), TagStore())

        verify(mockCapManager).onRegistrationStateChanged()
    }

}