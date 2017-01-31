package chat.willow.warren.extension.invite_notify

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import chat.willow.kale.IKale
import chat.willow.kale.irc.message.utility.CaseMapping
import chat.willow.warren.event.IWarrenEventDispatcher
import chat.willow.warren.extension.invite_notify.handler.InviteHandler
import chat.willow.warren.state.CaseMappingState
import chat.willow.warren.state.JoinedChannelsState
import org.junit.Before
import org.junit.Test

class InviteNotifyExtensionTests {

    private lateinit var sut: InviteNotifyExtension
    private lateinit var mockKale: IKale
    private lateinit var mockEventDispatcher: IWarrenEventDispatcher

    @Before fun setUp() {
        mockKale = mock()
        mockEventDispatcher = mock()

        sut = InviteNotifyExtension(mockKale, mockEventDispatcher)
    }

    @Test fun test_setUp_RegistersCorrectHandlers() {
        sut.setUp()

        verify(mockKale).register(any<InviteHandler>())
    }

    @Test fun test_tearDown_UnregistersCorrectHandlers() {
        sut.tearDown()

        verify(mockKale).unregister(any<InviteHandler>())
    }

}