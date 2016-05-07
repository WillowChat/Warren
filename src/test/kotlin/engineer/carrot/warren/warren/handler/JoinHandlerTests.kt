package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.irc.message.rfc1459.JoinMessage
import engineer.carrot.warren.kale.irc.prefix.Prefix
import engineer.carrot.warren.warren.state.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class JoinHandlerTests {

    lateinit var handler: JoinHandler
    lateinit var connectionState: ConnectionState
    lateinit var channelsState: ChannelsState

    @Before fun setUp() {
        val lifecycleState = LifecycleState.DISCONNECTED
        val capLifecycleState = CapLifecycle.NEGOTIATED
        val capState = CapState(lifecycle = capLifecycleState, negotiate = setOf(), server = mapOf(), accepted = setOf(), rejected = setOf())
        val saslState = SaslState(shouldAuth = false, lifecycle = SaslLifecycle.AUTH_FAILED, credentials = null)
        connectionState = ConnectionState(server = "test.server", port = 6697, nickname = "test-nick", username = "test-nick", lifecycle = lifecycleState, cap = capState, sasl = saslState)
        channelsState = ChannelsState(joined = mutableMapOf())
        handler = JoinHandler(connectionState, channelsState)
    }

    @Test fun test_handle_SourceIsSelf_WellFormed_JoinsCorrectChannel() {
        channelsState.joining["#channel"] = JoiningChannelState("#channel", status = JoiningChannelLifecycle.JOINING)

        handler.handle(JoinMessage(source = Prefix(nick = "test-nick"), channels = listOf("#channel")))

        assertEquals(ChannelsState(joined = mutableMapOf("#channel" to ChannelState(name = "#channel", users = generateUsers()))), channelsState)
    }

    @Test fun test_handle_SourceIsSelf_AlreadyInChannel() {
        channelsState.joined["#channel"] = ChannelState("#channel", users = generateUsers("test-nick"))

        handler.handle(JoinMessage(source = Prefix(nick = "test-nick"), channels = listOf("#channel")))

        assertEquals(ChannelsState(joined = mutableMapOf("#channel" to ChannelState(name = "#channel", users = generateUsers("test-nick")))), channelsState)
    }

    @Test fun test_handle_SourceIsSelf_MissingSource_DoesNothing() {
        handler.handle(JoinMessage(channels = listOf("#channel")))

        assertEquals(ChannelsState(joined = mutableMapOf()), channelsState)
    }

    @Test fun test_handle_SourceIsOther_WellFormed() {
        channelsState.joined["#channel"] = ChannelState("#channel", users = generateUsers("test-nick"))

        handler.handle(JoinMessage(source = Prefix(nick = "someone-else"), channels = listOf("#channel")))

        assertEquals(ChannelsState(joined = mutableMapOf("#channel" to ChannelState(name = "#channel", users = generateUsers("test-nick", "someone-else")))), channelsState)
    }

    @Test fun test_handle_SourceIsOther_AlreadyInChannel() {
        channelsState.joined["#channel"] = ChannelState("#channel", users = generateUsers("someone-else"))

        handler.handle(JoinMessage(source = Prefix(nick = "someone-else"), channels = listOf("#channel")))

        assertEquals(ChannelsState(joined = mutableMapOf("#channel" to ChannelState(name = "#channel", users = generateUsers("someone-else")))), channelsState)
    }

    @Test fun test_handle_SourceIsOther_NotInChannel() {
        handler.handle(JoinMessage(source = Prefix(nick = "someone-else"), channels = listOf("#channel")))

        assertEquals(ChannelsState(joined = mutableMapOf()), channelsState)
    }

}