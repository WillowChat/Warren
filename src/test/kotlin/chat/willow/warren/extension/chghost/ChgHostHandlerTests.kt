package chat.willow.warren.extension.chghost

import chat.willow.kale.irc.message.extension.account_notify.AccountMessage
import chat.willow.kale.irc.message.extension.chghost.ChgHostMessage
import chat.willow.kale.irc.message.utility.CaseMapping
import chat.willow.kale.irc.prefix.Prefix
import chat.willow.kale.irc.tag.TagStore
import chat.willow.warren.state.*
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class ChgHostHandlerTests {

    private lateinit var sut: ChgHostHandler
    private lateinit var channelsState: JoinedChannelsState

    @Before fun setUp() {
        channelsState = JoinedChannelsState(CaseMappingState(CaseMapping.RFC1459))

        sut = ChgHostHandler(channelsState)
    }

    @Test fun test_handle_UserHostUpdated_InAllChannels() {
        channelsState["#channel"] = ChannelState("#channel", users = generateUsersFromPrefixes(listOf(Prefix(nick = "nick", user = "olduser", host = "oldhost"))), topic = null)
        channelsState["#channel2"] = ChannelState("#channel2", users = generateUsersFromPrefixes(listOf(Prefix(nick = "nick", user = "olduser", host = "oldhost"))), topic = null)

        sut.handle(ChgHostMessage(source = Prefix(nick = "nick"), newUser = "newuser", newHost = "newhost"), TagStore())

        assertEquals(Prefix(nick = "nick", user = "newuser", host = "newhost"), channelsState["#channel"]!!.users["nick"]!!.prefix)
        assertEquals(Prefix(nick = "nick", user = "newuser", host = "newhost"), channelsState["#channel2"]!!.users["nick"]!!.prefix)
    }

    @Test fun test_handle_UserNotInChannel_NothingChanges() {
        val channel = ChannelState("#channel", users = generateUsersFromPrefixes(listOf(Prefix(nick = "other nick", user = "olduser", host = "oldhost"))), topic = null)
        channelsState["#channel"] = channel

        sut.handle(ChgHostMessage(source = Prefix(nick = "nick"), newUser = "newuser", newHost = "newhost"), TagStore())

        assertEquals(channel, channelsState["#channel"]!!)
    }

}