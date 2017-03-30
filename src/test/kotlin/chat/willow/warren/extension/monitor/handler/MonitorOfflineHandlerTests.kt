package chat.willow.warren.extension.monitor.handler

import chat.willow.kale.irc.message.extension.monitor.rpl.RplMonOffline
import chat.willow.kale.irc.prefix.Prefix
import chat.willow.kale.irc.tag.TagStore
import chat.willow.warren.event.IWarrenEvent
import chat.willow.warren.event.IWarrenEventDispatcher
import chat.willow.warren.extension.monitor.UserOfflineEvent
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

class MonitorOfflineHandlerTests {

    private lateinit var sut: MonitorOfflineHandler
    private lateinit var mockEventDispatcher: IWarrenEventDispatcher

    @Before fun setUp() {
        mockEventDispatcher = mock()

        sut = MonitorOfflineHandler(mockEventDispatcher)
    }

    @Test fun test_handle_NoTargets_FiresNoEvents() {
        sut.handle(RplMonOffline.Message(Prefix(nick = "me"), nickOrStar = "*", targets = listOf()), metadata = TagStore())

        verify(mockEventDispatcher, never()).fire(any<IWarrenEvent>())
    }

    @Test fun test_handle_WithTargets_FiresEventForEachUser() {
        val userOne = Prefix(nick = "user1", host = "somewhere")
        val userTwo = Prefix(nick = "user2", user = "user2")
        val targets = listOf(userOne, userTwo)

        sut.handle(RplMonOffline.Message(Prefix(nick = "me"), nickOrStar = "*", targets = targets), metadata = TagStore())

        inOrder(mockEventDispatcher) {
            verify(mockEventDispatcher).fire(UserOfflineEvent(prefix = userOne))
            verify(mockEventDispatcher).fire(UserOfflineEvent(prefix = userTwo))
        }
    }
    
}