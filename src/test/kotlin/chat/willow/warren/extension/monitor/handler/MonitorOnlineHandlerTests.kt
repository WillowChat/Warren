package chat.willow.warren.extension.monitor.handler

import chat.willow.kale.irc.message.extension.monitor.rpl.RplMonOnline
import chat.willow.kale.irc.prefix.Prefix
import chat.willow.kale.irc.tag.TagStore
import chat.willow.warren.event.IWarrenEvent
import chat.willow.warren.event.IWarrenEventDispatcher
import chat.willow.warren.extension.monitor.UserOnlineEvent
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class MonitorOnlineHandlerTests {

    private lateinit var sut: MonitorOnlineHandler
    private lateinit var mockEventDispatcher: IWarrenEventDispatcher

    @Before fun setUp() {
        mockEventDispatcher = mock()

        sut = MonitorOnlineHandler(mockEventDispatcher)
    }

    @Test fun test_handle_NoTargets_FiresNoEvents() {
        sut.handle(RplMonOnline.Message(Prefix(nick = "me"), nickOrStar = "*", targets = listOf()), metadata = TagStore())

        Mockito.verify(mockEventDispatcher, Mockito.never()).fire(any<IWarrenEvent>())
    }

    @Test fun test_handle_WithTargets_FiresEventForEachUser() {
        val userOne = Prefix(nick = "user1", host = "somewhere")
        val userTwo = Prefix(nick = "user2", user = "user2")
        val targets = listOf(userOne, userTwo)

        sut.handle(RplMonOnline.Message(Prefix(nick = "me"), nickOrStar = "*", targets = targets), metadata = TagStore())

        inOrder(mockEventDispatcher) {
            verify(mockEventDispatcher).fire(UserOnlineEvent(prefix = userOne))
            verify(mockEventDispatcher).fire(UserOnlineEvent(prefix = userTwo))
        }
    }

}