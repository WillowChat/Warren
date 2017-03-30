package chat.willow.warren.extension.invite_notify.handler

import chat.willow.kale.irc.message.rfc1459.InviteMessage
import chat.willow.kale.irc.prefix.Prefix
import chat.willow.kale.irc.tag.TagStore
import chat.willow.warren.event.IWarrenEventDispatcher
import chat.willow.warren.event.InvitedToChannelEvent
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test

class InviteHandlerTests {

    private lateinit var sut: InviteHandler
    private lateinit var mockEventDispatcher: IWarrenEventDispatcher

    @Before fun setUp() {
        mockEventDispatcher = mock()

        sut = InviteHandler(mockEventDispatcher)
    }

    @Test fun test_handle_MessageWithSource_FiresEventForInviteToChannel() {
        val message = InviteMessage.Message(source = Prefix(nick = "test-user"), user = "anybody", channel = "#somewhere")

        sut.handle(message, metadata = TagStore())

        verify(mockEventDispatcher).fire(InvitedToChannelEvent(source = Prefix(nick = "test-user"), channel = "#somewhere"))
    }

}