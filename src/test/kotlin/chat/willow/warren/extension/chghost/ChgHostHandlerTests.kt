package chat.willow.warren.extension.chghost

import chat.willow.kale.helper.CaseMapping
import chat.willow.kale.irc.message.extension.chghost.ChgHostMessage
import chat.willow.kale.irc.prefix.Prefix
import chat.willow.kale.irc.tag.TagStore
import chat.willow.warren.state.CaseMappingState
import chat.willow.warren.state.ChannelState
import chat.willow.warren.state.JoinedChannelsState
import chat.willow.warren.state.generateUsersFromPrefixes
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ChgHostHandlerTests {

    private lateinit var sut: ChgHostHandler
    private lateinit var channelsState: JoinedChannelsState

    @Before fun setUp() {
        channelsState = JoinedChannelsState(CaseMappingState(CaseMapping.RFC1459))

        sut = ChgHostHandler(channelsState)
    }

    @Test fun test_handle_UserHostUpdated_InAllChannels() {
        channelsState += ChannelState("#channel", users = generateUsersFromPrefixes(listOf(Prefix(nick = "nick", user = "olduser", host = "oldhost"))), topic = null)
        channelsState += ChannelState("#channel2", users = generateUsersFromPrefixes(listOf(Prefix(nick = "nick", user = "olduser", host = "oldhost"))), topic = null)

        sut.handle(ChgHostMessage.Message(source = Prefix(nick = "nick"), newUser = "newuser", newHost = "newhost"), TagStore())

        assertEquals(Prefix(nick = "nick", user = "newuser", host = "newhost"), channelsState["#channel"]!!.users["nick"]!!.prefix)
        assertEquals(Prefix(nick = "nick", user = "newuser", host = "newhost"), channelsState["#channel2"]!!.users["nick"]!!.prefix)
    }

    @Test fun test_handle_UserNotInChannel_NothingChanges() {
        val channel = ChannelState("#channel", users = generateUsersFromPrefixes(listOf(Prefix(nick = "other nick", user = "olduser", host = "oldhost"))), topic = null)
        channelsState += channel

        sut.handle(ChgHostMessage.Message(source = Prefix(nick = "nick"), newUser = "newuser", newHost = "newhost"), TagStore())

        assertEquals(channel, channelsState["#channel"]!!)
    }

}