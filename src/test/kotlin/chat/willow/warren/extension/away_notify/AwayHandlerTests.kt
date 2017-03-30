package chat.willow.warren.extension.away_notify

import chat.willow.kale.helper.CaseMapping
import chat.willow.kale.irc.message.extension.away_notify.AwayMessage
import chat.willow.kale.irc.prefix.Prefix
import chat.willow.kale.irc.tag.TagStore
import chat.willow.warren.state.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AwayHandlerTests {

    private lateinit var sut: AwayHandler
    private lateinit var channelsState: JoinedChannelsState

    @Before fun setUp() {
        channelsState = JoinedChannelsState(CaseMappingState(CaseMapping.RFC1459))

        sut = AwayHandler(channelsState)
    }

    @Test fun test_handle_TrackedUser_AwayMessageSet_UpdatedInAllChannels() {
        channelsState += ChannelState("#channel", users = generateUsersFromNicks(listOf("test-user")), topic = null)
        channelsState += ChannelState("#channel2", users = generateUsersFromNicks(listOf("test-user")), topic = null)

        val message = AwayMessage.Message(source = Prefix(nick = "test-user"), message = "away message")

        sut.handle(message, metadata = TagStore())

        assertEquals("away message", channelsState["#channel"]!!.users["test-user"]!!.awayMessage)
        assertEquals("away message", channelsState["#channel2"]!!.users["test-user"]!!.awayMessage)
    }

    @Test fun test_handle_NonTrackedUser_AwayMessageSet_NothingChanges() {
        val someoneElse = generateUser("someone-else", awayMessage = null)
        channelsState += ChannelState("#channel", users = generateChannelUsersState(someoneElse), topic = null)

        val message = AwayMessage.Message(source = Prefix(nick = "test-user"), message = "away message")

        sut.handle(message, metadata = TagStore())

        assertNull(channelsState["#channel"]!!.users["someone-else"]!!.awayMessage)
    }

    @Test fun test_handle_TrackedUser_AwayMessageRemoved_UpdatedInAllChannels() {
        channelsState += ChannelState("#channel", users = generateUsersFromNicks(listOf("test-user")), topic = null)
        channelsState += ChannelState("#channel2", users = generateUsersFromNicks(listOf("test-user")), topic = null)

        val message = AwayMessage.Message(source = Prefix(nick = "test-user"), message = null)

        sut.handle(message, metadata = TagStore())

        assertNull(channelsState["#channel"]!!.users["test-user"]!!.awayMessage)
        assertNull(channelsState["#channel2"]!!.users["test-user"]!!.awayMessage)
    }

    @Test fun test_handle_NonTrackedUser_AwayMessageRemoved_NothingChanges() {
        val someoneElse = generateUser("someone-else", awayMessage = "away message")
        channelsState += ChannelState("#channel", users = generateChannelUsersState(someoneElse), topic = null)

        val message = AwayMessage.Message(source = Prefix(nick = "test-user"), message = null)

        sut.handle(message, metadata = TagStore())

        assertEquals("away message", channelsState["#channel"]!!.users["someone-else"]!!.awayMessage)
    }

}