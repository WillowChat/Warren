package chat.willow.warren.extension.sasl

import chat.willow.kale.IKale
import chat.willow.kale.irc.message.extension.sasl.AuthenticateMessage
import chat.willow.warren.IMessageSink
import chat.willow.warren.extension.cap.CapLifecycle
import chat.willow.warren.extension.cap.CapState
import chat.willow.warren.extension.cap.ICapManager
import chat.willow.warren.state.AuthLifecycle
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SaslExtensionTests {

    private lateinit var sut: SaslExtension
    private lateinit var mockKale: IKale
    private lateinit var capState: CapState
    private lateinit var mockSink: IMessageSink
    private lateinit var mockCapManager: ICapManager

    @Before fun setUp() {
        mockKale = mock()
        mockSink = mock()
        mockCapManager = mock()

        val capLifecycleState = CapLifecycle.NEGOTIATED
        capState = CapState(lifecycle = capLifecycleState, negotiate = setOf(), server = mapOf(), accepted = setOf(), rejected = setOf())

        sut = SaslExtension(SaslState(shouldAuth = false, lifecycle = AuthLifecycle.NO_AUTH, credentials = null), mockKale, mockCapManager, mockSink)
    }

    @Test fun test_setUp_RegistersCorrectHandlers() {
        sut.setUp()

        verify(mockKale).register(any<AuthenticateHandler>())
        verify(mockKale).register(any<Rpl903Handler>())
        verify(mockKale).register(any<Rpl904Handler>())
        verify(mockKale).register(any<Rpl905Handler>())
    }

    @Test fun test_setUp_ShouldAuth_ChangesSaslLifecycleToAuthing() {
        sut.internalState.shouldAuth = true

        sut.setUp()

        Assert.assertEquals(AuthLifecycle.AUTHING, sut.internalState.lifecycle)
    }

    @Test fun test_setUp_ShouldAuth_WriteAuthenticatePlainMessage() {
        sut.internalState.shouldAuth = true

        sut.setUp()

        verify(mockSink).write(AuthenticateMessage(payload = "PLAIN", isEmpty = false))
    }

    @Test fun test_setUp_ShouldAuth_NoMechanisms_WriteAuthenticatePlainMessage() {
        sut.internalState.shouldAuth = true
        sut.internalState.mechanisms = setOf()

        sut.setUp()

        verify(mockSink).write(AuthenticateMessage(payload = "PLAIN", isEmpty = false))
    }

    @Test fun test_setUp_ShouldAuth_PLAINNotSupported_DoesNotWriteAuthenticatePlainMessage() {
        sut.internalState.shouldAuth = true
        sut.internalState.mechanisms = setOf("something else")

        sut.setUp()

        verify(mockSink, never()).write(any<AuthenticateMessage>())
    }

    @Test fun test_setUp_ShouldNotAuth_DoesNotWriteAuthenticatePlainMessage() {
        sut.internalState.shouldAuth = false

        sut.setUp()

        verify(mockSink, never()).write(any<AuthenticateMessage>())
    }

    @Test fun test_tearDown_UnregistersCorrectHandlers() {
        sut.tearDown()

        verify(mockKale).unregister(any<AuthenticateHandler>())
        verify(mockKale).unregister(any<Rpl903Handler>())
        verify(mockKale).unregister(any<Rpl904Handler>())
        verify(mockKale).unregister(any<Rpl905Handler>())
    }

    @Test fun test_tearDown_SetsAuthLifecycleToAuthing() {
        sut.tearDown()

        assertEquals(AuthLifecycle.AUTHING, sut.internalState.lifecycle)
    }

    @Test fun test_tearDown_SetsMechanismsToEmpty() {
        sut.tearDown()

        assertEquals(emptySet<String>(), sut.internalState.mechanisms)
    }

    @Test fun test_valueSet_Null_SetsMechanismsToEmpty() {
        sut.valueSet(null)

        assertEquals(emptySet<String>(), sut.internalState.mechanisms)
    }

    @Test fun test_valueSet_Multiple_SetsMechanismsAccordingly() {
        sut.valueSet("mech1,mech2,mech3")

        assertEquals(setOf("mech1", "mech2", "mech3"), sut.internalState.mechanisms)
    }

}