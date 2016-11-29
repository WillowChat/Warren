package engineer.carrot.warren.warren.registration

import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.never

class RegistrationManagerTests {

    private lateinit var sut: RegistrationManager
    private lateinit var mockListener: IRegistrationListener

    @Before fun setUp() {
        sut = RegistrationManager()
        mockListener = mock()

        sut.listener = mockListener
    }

    @Test fun test_register_DoesNotStartExtension() {
        val extension = mock<IRegistrationExtension>()

        sut.register(extension)

        verify(extension, never()).startRegistration()
    }

    @Test fun test_startRegistration_StartsAllRegisteredExtensions_InOrder() {
        val extensionOne = mock<IRegistrationExtension>()
        val extensionTwo = mock<IRegistrationExtension>()

        sut.register(extensionOne)
        sut.register(extensionTwo)
        sut.startRegistration()

        inOrder(extensionOne, extensionTwo) {
            verify(extensionOne).startRegistration()
            verify(extensionTwo).startRegistration()
        }
    }

    @Test fun test_onExtensionSuccess_SingleExtensionRegistered_TellsListenerRegistrationEnded() {
        val extension = MockRegistrationExtension(sut)
        sut.register(extension)

        sut.onExtensionSuccess(extension)

        verify(mockListener).onRegistrationEnded()
    }

    @Test fun test_onExtensionFailure_SingleExtensionRegistered_TellsListenerRegistrationFailed() {
        val extension = MockRegistrationExtension(sut)
        sut.register(extension)

        sut.onExtensionFailure(extension)

        verify(mockListener).onRegistrationFailed()
    }

    @Test fun test_onExtensionSuccess_MultipleExtensionsStarted_DoesNothing() {
        val extensionOne = MockRegistrationExtension(sut)
        val extensionTwo = MockRegistrationExtension(sut)
        sut.register(extensionOne)
        sut.register(extensionTwo)

        sut.onExtensionSuccess(extensionOne)

        verify(mockListener, never()).onRegistrationEnded()
        verify(mockListener, never()).onRegistrationFailed()
    }

    private class MockRegistrationExtension(val registrationManager: IRegistrationManager) : IRegistrationExtension {
        override fun startRegistration() {
            // NO-OP
        }

        override fun onRegistrationSucceeded() {
            registrationManager.onExtensionSuccess(this)
        }

        override fun onRegistrationFailed() {
            registrationManager.onExtensionFailure(this)
        }
    }

}
