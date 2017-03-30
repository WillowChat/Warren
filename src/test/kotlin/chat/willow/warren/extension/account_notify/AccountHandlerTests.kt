package chat.willow.warren.extension.account_notify

import chat.willow.kale.helper.CaseMapping
import chat.willow.kale.irc.message.extension.account_notify.AccountMessage
import chat.willow.kale.irc.prefix.Prefix
import chat.willow.kale.irc.tag.TagStore
import chat.willow.warren.state.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AccountHandlerTests {

    private lateinit var sut: AccountHandler
    private lateinit var channelsState: JoinedChannelsState

    @Before fun setUp() {
        channelsState = JoinedChannelsState(CaseMappingState(CaseMapping.RFC1459))

        sut = AccountHandler(channelsState)
    }

    @Test fun test_handle_TrackedUser_LoggedOut_UpdatesUserInAllChannels() {
        channelsState += ChannelState("#channel", users = generateUsersFromNicks(listOf("test-user")), topic = null)
        channelsState += ChannelState("#channel2", users = generateUsersFromNicks(listOf("test-user")), topic = null)

        val message = AccountMessage.Message(source = Prefix(nick = "test-user"), account = "*")

        sut.handle(message, metadata = TagStore())

        assertNull(channelsState["#channel"]!!.users["test-user"]!!.account)
        assertNull(channelsState["#channel2"]!!.users["test-user"]!!.account)
    }

    @Test fun test_handle_TrackedUser_LoggedIn_UpdatesUserInAllChannels() {
        channelsState += ChannelState("#channel", users = generateUsersFromNicks(listOf("test-user")), topic = null)
        channelsState += ChannelState("#channel2", users = generateUsersFromNicks(listOf("test-user")), topic = null)

        val message = AccountMessage.Message(source = Prefix(nick = "test-user"), account = "test-account")

        sut.handle(message, metadata = TagStore())

        assertEquals("test-account", channelsState["#channel"]!!.users["test-user"]!!.account)
        assertEquals("test-account", channelsState["#channel2"]!!.users["test-user"]!!.account)
    }

    @Test fun test_handle_NonTrackedUser_LoggedOut_NothingChanges() {
        val someoneElseAccount = generateUser("someone-else", account = "someone-account")
        channelsState += ChannelState("#channel", users = generateChannelUsersState(someoneElseAccount), topic = null)

        val message = AccountMessage.Message(source = Prefix(nick = "test-user"), account = "*")

        sut.handle(message, metadata = TagStore())

        assertEquals("someone-account", channelsState["#channel"]!!.users["someone-else"]!!.account)
    }

    @Test fun test_handle_NonTrackedUser_LoggedIn_NothingChanges() {
        val someoneElseAccount = generateUser("someone-else", account = "someone-account")
        channelsState += ChannelState("#channel", users = generateChannelUsersState(someoneElseAccount), topic = null)

        val message = AccountMessage.Message(source = Prefix(nick = "test-user"), account = "test-user-account")

        sut.handle(message, metadata = TagStore())

        assertEquals("someone-account", channelsState["#channel"]!!.users["someone-else"]!!.account)
    }

}