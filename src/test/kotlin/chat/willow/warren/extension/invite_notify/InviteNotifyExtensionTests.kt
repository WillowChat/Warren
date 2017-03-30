package chat.willow.warren.extension.invite_notify

import chat.willow.kale.IKaleIrcMessageHandler
import chat.willow.kale.IKaleRouter
import chat.willow.warren.event.IWarrenEventDispatcher
import chat.willow.warren.extension.invite_notify.handler.InviteHandler
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test

class InviteNotifyExtensionTests {

    private lateinit var sut: InviteNotifyExtension
    private lateinit var mockKaleRouter: IKaleRouter<IKaleIrcMessageHandler>
    private lateinit var mockEventDispatcher: IWarrenEventDispatcher

    @Before fun setUp() {
        mockKaleRouter = mock()
        mockEventDispatcher = mock()

        sut = InviteNotifyExtension(mockKaleRouter, mockEventDispatcher)
    }

    @Test fun test_setUp_RegistersCorrectHandlers() {
        sut.setUp()

        verify(mockKaleRouter).register(eq("INVITE"), any<InviteHandler>())
    }

    @Test fun test_tearDown_UnregistersCorrectHandlers() {
        sut.tearDown()

        verify(mockKaleRouter).unregister("INVITE")
    }

}