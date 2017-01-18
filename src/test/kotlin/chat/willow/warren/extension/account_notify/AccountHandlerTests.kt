package chat.willow.warren.extension.account_notify

import engineer.carrot.warren.kale.irc.message.extension.account_notify.AccountMessage
import engineer.carrot.warren.kale.irc.message.utility.CaseMapping
import engineer.carrot.warren.kale.irc.prefix.Prefix
import chat.willow.warren.state.*
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class AccountHandlerTests {

    private lateinit var sut: AccountHandler
    private lateinit var channelsState: JoinedChannelsState

    @Before fun setUp() {
        channelsState = JoinedChannelsState(CaseMappingState(CaseMapping.RFC1459))

        sut = AccountHandler(channelsState)
    }

    @Test fun test_handle_TrackedUser_LoggedOut_UpdatesUserInAllChannels() {
        channelsState["#channel"] = ChannelState("#channel", users = generateUsers("test-user"), topic = null)
        channelsState["#channel2"] = ChannelState("#channel2", users = generateUsers("test-user"), topic = null)

        val message = AccountMessage(source = Prefix(nick = "test-user"), account = "*")

        sut.handle(message, tags = mapOf())

        assertNull(channelsState["#channel"]!!.users["test-user"]!!.account)
        assertNull(channelsState["#channel2"]!!.users["test-user"]!!.account)
    }

    @Test fun test_handle_TrackedUser_LoggedIn_UpdatesUserInAllChannels() {
        channelsState["#channel"] = ChannelState("#channel", users = generateUsers("test-user"), topic = null)
        channelsState["#channel2"] = ChannelState("#channel2", users = generateUsers("test-user"), topic = null)

        val message = AccountMessage(source = Prefix(nick = "test-user"), account = "test-account")

        sut.handle(message, tags = mapOf())

        assertEquals("test-account", channelsState["#channel"]!!.users["test-user"]!!.account)
        assertEquals("test-account", channelsState["#channel2"]!!.users["test-user"]!!.account)
    }

    @Test fun test_handle_NonTrackedUser_LoggedOut_NothingChanges() {
        val someoneElseAccount = generateUser("someone-else", account = "someone-account")
        channelsState["#channel"] = ChannelState("#channel", users = generateChannelUsersState(someoneElseAccount), topic = null)

        val message = AccountMessage(source = Prefix(nick = "test-user"), account = "*")

        sut.handle(message, tags = mapOf())

        assertEquals("someone-account", channelsState["#channel"]!!.users["someone-else"]!!.account)
    }

    @Test fun test_handle_NonTrackedUser_LoggedIn_NothingChanges() {
        val someoneElseAccount = generateUser("someone-else", account = "someone-account")
        channelsState["#channel"] = ChannelState("#channel", users = generateChannelUsersState(someoneElseAccount), topic = null)

        val message = AccountMessage(source = Prefix(nick = "test-user"), account = "test-user-account")

        sut.handle(message, tags = mapOf())

        assertEquals("someone-account", channelsState["#channel"]!!.users["someone-else"]!!.account)
    }

}