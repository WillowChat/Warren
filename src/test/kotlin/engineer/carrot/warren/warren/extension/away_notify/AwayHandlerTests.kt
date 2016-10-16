package engineer.carrot.warren.warren.extension.away_notify

import engineer.carrot.warren.kale.irc.message.extension.away_notify.AwayMessage
import engineer.carrot.warren.kale.irc.message.utility.CaseMapping
import engineer.carrot.warren.kale.irc.prefix.Prefix
import engineer.carrot.warren.warren.state.*
import org.junit.Assert.*
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
        channelsState["#channel"] = ChannelState("#channel", users = generateUsers("test-user"), topic = null)
        channelsState["#channel2"] = ChannelState("#channel2", users = generateUsers("test-user"), topic = null)

        val message = AwayMessage(source = Prefix(nick = "test-user"), message = "away message")

        sut.handle(message, tags = mapOf())

        assertEquals("away message", channelsState["#channel"]!!.users["test-user"]!!.awayMessage)
        assertEquals("away message", channelsState["#channel2"]!!.users["test-user"]!!.awayMessage)
    }

    @Test fun test_handle_NonTrackedUser_AwayMessageSet_NothingChanges() {
        val someoneElse = generateUser("someone-else", awayMessage = null)
        channelsState["#channel"] = ChannelState("#channel", users = generateChannelUsersState(someoneElse), topic = null)

        val message = AwayMessage(source = Prefix(nick = "test-user"), message = "away message")

        sut.handle(message, tags = mapOf())

        assertNull(channelsState["#channel"]!!.users["someone-else"]!!.awayMessage)
    }

    @Test fun test_handle_TrackedUser_AwayMessageRemoved_UpdatedInAllChannels() {
        channelsState["#channel"] = ChannelState("#channel", users = generateUsers("test-user"), topic = null)
        channelsState["#channel2"] = ChannelState("#channel2", users = generateUsers("test-user"), topic = null)

        val message = AwayMessage(source = Prefix(nick = "test-user"), message = null)

        sut.handle(message, tags = mapOf())

        assertNull(channelsState["#channel"]!!.users["test-user"]!!.awayMessage)
        assertNull(channelsState["#channel2"]!!.users["test-user"]!!.awayMessage)
    }

    @Test fun test_handle_NonTrackedUser_AwayMessageRemoved_NothingChanges() {
        val someoneElse = generateUser("someone-else", awayMessage = "away message")
        channelsState["#channel"] = ChannelState("#channel", users = generateChannelUsersState(someoneElse), topic = null)

        val message = AwayMessage(source = Prefix(nick = "test-user"), message = null)

        sut.handle(message, tags = mapOf())

        assertEquals("away message", channelsState["#channel"]!!.users["someone-else"]!!.awayMessage)
    }

}