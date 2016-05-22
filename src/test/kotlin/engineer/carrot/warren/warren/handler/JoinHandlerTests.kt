package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.irc.message.rfc1459.JoinMessage
import engineer.carrot.warren.kale.irc.message.utility.CaseMapping
import engineer.carrot.warren.kale.irc.prefix.Prefix
import engineer.carrot.warren.warren.state.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class JoinHandlerTests {

    lateinit var handler: JoinHandler
    lateinit var connectionState: ConnectionState
    lateinit var channelsState: ChannelsState
    val caseMappingState = CaseMappingState(mapping = CaseMapping.RFC1459)

    @Before fun setUp() {
        val lifecycleState = LifecycleState.DISCONNECTED
        val capLifecycleState = CapLifecycle.NEGOTIATED
        val capState = CapState(lifecycle = capLifecycleState, negotiate = setOf(), server = mapOf(), accepted = setOf(), rejected = setOf())
        val saslState = SaslState(shouldAuth = false, lifecycle = SaslLifecycle.AUTH_FAILED, credentials = null)
        connectionState = ConnectionState(server = "test.server", port = 6697, nickname = "test-nick", username = "test-nick", lifecycle = lifecycleState, cap = capState, sasl = saslState)
        channelsState = channelsStateWith(listOf(), caseMappingState)
        handler = JoinHandler(connectionState, channelsState.joining, channelsState.joined, caseMappingState)
    }

    @Test fun test_handle_SourceIsSelf_WellFormed_JoinsCorrectChannel() {
        channelsState.joining += JoiningChannelState("#channel", status = JoiningChannelLifecycle.JOINING)

        handler.handle(JoinMessage(source = Prefix(nick = "test-nick"), channels = listOf("#channel")))

        assertEquals(channelsStateWith(listOf(ChannelState(name = "#channel", users = generateUsers())), caseMappingState), channelsState)
    }

    @Test fun test_handle_SourceIsSelf_AlreadyInChannel() {
        channelsState.joined += ChannelState("#channel", users = generateUsers("test-nick"))

        handler.handle(JoinMessage(source = Prefix(nick = "test-nick"), channels = listOf("#channel")))

        assertEquals(channelsStateWith(listOf(ChannelState(name = "#channel", users = generateUsers("test-nick"))), caseMappingState), channelsState)
    }

    @Test fun test_handle_SourceIsSelf_MissingSource_DoesNothing() {
        handler.handle(JoinMessage(channels = listOf("#channel")))

        assertEquals(channelsStateWith(listOf(), caseMappingState), channelsState)
    }

    @Test fun test_handle_SourceIsOther_WellFormed() {
        channelsState.joined += ChannelState("#channel", users = generateUsers("test-nick"))

        handler.handle(JoinMessage(source = Prefix(nick = "someone-else"), channels = listOf("#channel")))

        assertEquals(channelsStateWith(listOf(ChannelState(name = "#channel", users = generateUsers("test-nick", "someone-else"))), caseMappingState), channelsState)
    }

    @Test fun test_handle_SourceIsOther_AlreadyInChannel() {
        channelsState.joined += ChannelState("#channel", users = generateUsers("someone-else"))

        handler.handle(JoinMessage(source = Prefix(nick = "someone-else"), channels = listOf("#channel")))

        assertEquals(channelsStateWith(listOf(ChannelState(name = "#channel", users = generateUsers("someone-else"))), caseMappingState), channelsState)
    }

    @Test fun test_handle_SourceIsOther_NotInChannel() {
        handler.handle(JoinMessage(source = Prefix(nick = "someone-else"), channels = listOf("#channel")))

        assertEquals(channelsStateWith(listOf(), caseMappingState), channelsState)
    }

}