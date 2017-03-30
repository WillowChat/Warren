package chat.willow.warren.extension.extended_join

import chat.willow.kale.IKaleIrcMessageHandler
import chat.willow.kale.IKaleMessageHandler
import chat.willow.kale.helper.CaseMapping
import chat.willow.kale.irc.message.extension.extended_join.ExtendedJoinMessage
import chat.willow.kale.irc.message.rfc1459.JoinMessage
import chat.willow.kale.irc.prefix.Prefix
import chat.willow.kale.irc.tag.TagStore
import chat.willow.warren.state.*
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ExtendedJoinHandlerTests {

    private lateinit var sut: ExtendedJoinHandler
    private lateinit var channelsState: ChannelsState
    private lateinit var connectionState: ConnectionState
    private lateinit var mockJoinHandler: IKaleMessageHandler<JoinMessage.Message>
    private lateinit var mockJoinIrcMessageHandler: IKaleIrcMessageHandler

    @Before fun setUp() {
        val lifecycleState = LifecycleState.DISCONNECTED
        val caseMappingState = CaseMappingState(CaseMapping.RFC1459)

        mockJoinHandler = mock()
        mockJoinIrcMessageHandler = mock()
        channelsState = emptyChannelsState(caseMappingState)
        connectionState = ConnectionState(server = "test.server", port = 6697, nickname = "test-nick", user = "test-nick", lifecycle = lifecycleState)

        sut = ExtendedJoinHandler(mockJoinHandler, channelsState, caseMappingState)
    }


    @Test fun test_handle_CallsJoinHandler_WithCorrectMessage() {
        val message = ExtendedJoinMessage.Message(source = Prefix(nick = "test-user"), channel = "#channel", account = "test-account", realName = "real name")

        sut.handle(message, metadata = TagStore())

        verify(mockJoinHandler).handle(message = JoinMessage.Message(source = Prefix(nick = "test-user"), channels = listOf("#channel")), metadata = TagStore())
    }

    @Test fun test_handle_CallsJoinHandler_WithCorrectTags() {
        val message = ExtendedJoinMessage.Message(source = Prefix(nick = "test-user"), channel = "#channel", account = "test-account", realName = "real name")

        val tagStore = TagStore()
        sut.handle(message, metadata = tagStore)

        verify(mockJoinHandler).handle(message = JoinMessage.Message(source = Prefix(nick = "test-user"), channels = listOf("#channel")), metadata = tagStore)
    }

    @Test fun test_handle_JoinedChannel_UserAccountUpdatedInAllChannels() {
        val testUser = generateUser("test-user", account = null)
        channelsState.joined += ChannelState("#channel", users = generateChannelUsersState(testUser))
        channelsState.joined += ChannelState("#channel2", users = generateChannelUsersState(testUser))

        val message = ExtendedJoinMessage.Message(source = Prefix(nick = "test-user"), channel = "#channel", account = "test-account", realName = "real name")

        sut.handle(message, metadata = TagStore())

        assertEquals("test-account", channelsState.joined["#channel"]!!.users["test-user"]!!.account)
        assertEquals("test-account", channelsState.joined["#channel2"]!!.users["test-user"]!!.account)
    }

    @Test fun test_handle_JoinedChannel_NonTrackedUser_NothingChanges() {
        val someoneElseUser = generateUser("someone-else", account = "someone-account")
        channelsState.joined += ChannelState("#channel", users = generateChannelUsersState(someoneElseUser))
        channelsState.joined += ChannelState("#channel2", users = generateChannelUsersState(someoneElseUser))

        val message = ExtendedJoinMessage.Message(source = Prefix(nick = "test-user"), channel = "#channel", account = "test-account", realName = "real name")

        sut.handle(message, metadata = TagStore())

        assertEquals("someone-account", channelsState.joined["#channel"]!!.users["someone-else"]!!.account)
        assertEquals("someone-account", channelsState.joined["#channel2"]!!.users["someone-else"]!!.account)
    }

}