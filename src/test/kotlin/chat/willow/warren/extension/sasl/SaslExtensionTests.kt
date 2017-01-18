package chat.willow.warren.extension.sasl

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import chat.willow.kale.IKale
import chat.willow.warren.IMessageSink
import chat.willow.warren.extension.cap.CapLifecycle
import chat.willow.warren.extension.cap.CapState
import chat.willow.warren.extension.cap.ICapManager
import chat.willow.warren.state.AuthLifecycle
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

    @Test fun test_tearDown_UnregistersCorrectHandlers() {
        sut.tearDown()

        verify(mockKale).unregister(any<AuthenticateHandler>())
        verify(mockKale).unregister(any<Rpl903Handler>())
        verify(mockKale).unregister(any<Rpl904Handler>())
        verify(mockKale).unregister(any<Rpl905Handler>())
    }


}