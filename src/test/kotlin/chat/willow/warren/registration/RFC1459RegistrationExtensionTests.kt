package chat.willow.warren.registration

import chat.willow.kale.irc.message.rfc1459.NickMessage
import chat.willow.kale.irc.message.rfc1459.PassMessage
import chat.willow.kale.irc.message.rfc1459.UserMessage
import chat.willow.warren.IMessageSink
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.never

class RFC1459RegistrationExtensionTests {

    private lateinit var sut: RFC1459RegistrationExtension
    private lateinit var mockSink: IMessageSink
    private lateinit var mockRegistrationManager: IRegistrationManager

    @Before fun setUp() {
        mockSink = mock()
        mockRegistrationManager = mock()

        val nickname = "test-user"
        val username = "test-user"
        val password = null

        sut = RFC1459RegistrationExtension(mockSink, nickname, username, password, mockRegistrationManager)
    }

    @Test fun test_startRegistration_PasswordIsNull_DoesNotWritePassMessage() {
        val sut = RFC1459RegistrationExtension(mockSink, "", "", password = null, registrationManager = mockRegistrationManager)

        sut.startRegistration()

        verify(mockSink, never()).write(any<PassMessage>())
    }

    @Test fun test_startRegistration_PasswordIsNotNull_WritesPassMessageFirst() {
        val sut = RFC1459RegistrationExtension(mockSink, "", "", password = "something", registrationManager = mockRegistrationManager)

        sut.startRegistration()

        inOrder(mockSink) {
            verify(mockSink).write(PassMessage.Command(password = "something"))
            verify(mockSink).write(any<NickMessage.Command>())
        }
    }

    @Test fun test_startRegistration_WritesNickAndUser_InOrder() {
        sut.startRegistration()

        inOrder(mockSink) {
            verify(mockSink).write(NickMessage.Command(nickname = "test-user"))
            verify(mockSink).write(UserMessage.Command(username = "test-user", mode = "8", realname = "test-user"))
        }
    }

    @Test fun test_onRegistrationSucceeded_TellsRegistrationManager() {
        sut.onRegistrationSucceeded()

        verify(mockRegistrationManager).onExtensionSuccess(sut)
    }

    @Test fun test_onRegistrationFailed_TellsRegistrationManager() {
        sut.onRegistrationFailed()

        verify(mockRegistrationManager).onExtensionFailure(sut)
    }

}