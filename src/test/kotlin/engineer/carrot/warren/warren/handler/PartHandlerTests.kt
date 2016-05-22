package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.irc.message.rfc1459.PartMessage
import engineer.carrot.warren.kale.irc.message.utility.CaseMapping
import engineer.carrot.warren.kale.irc.prefix.Prefix
import engineer.carrot.warren.warren.state.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PartHandlerTests {

    lateinit var handler: PartHandler
    lateinit var connectionState: ConnectionState
    lateinit var channelsState: ChannelsState
    val caseMappingState = CaseMappingState(mapping = CaseMapping.RFC1459)

    @Before fun setUp() {
        val lifecycleState = LifecycleState.DISCONNECTED
        val capLifecycleState = CapLifecycle.NEGOTIATED
        val capState = CapState(lifecycle = capLifecycleState, negotiate = setOf(), server = mapOf(), accepted = setOf(), rejected = setOf())
        val saslState = SaslState(shouldAuth = false, lifecycle = SaslLifecycle.AUTH_FAILED, credentials = null)
        connectionState = ConnectionState(server = "test.server", port = 6697, nickname = "test-nick", username = "test-nick", lifecycle = lifecycleState, cap = capState, sasl = saslState)
        channelsState = emptyChannelsState(caseMappingState)
        handler = PartHandler(connectionState, channelsState.joined, caseMappingState)
    }

    @Test fun test_handle_SourceIsSelf_WellFormed_PartsCorrectChannel() {
        channelsState.joined += ChannelState("#channel", users = generateUsers("test-nick"))

        handler.handle(PartMessage(source = Prefix(nick = "test-nick"), channels = listOf("#channel")))

        assertEquals(emptyChannelsState(caseMappingState), channelsState)
    }

    @Test fun test_handle_SourceIsSelf_NotInChannel() {
        handler.handle(PartMessage(source = Prefix(nick = "test-nick"), channels = listOf("#channel")))

        assertEquals(emptyChannelsState(caseMappingState), channelsState)
    }

    @Test fun test_handle_SourceIsSelf_MissingSource_DoesNothing() {
        handler.handle(PartMessage(channels = listOf("#channel")))

        assertEquals(emptyChannelsState(caseMappingState), channelsState)
    }

    @Test fun test_handle_SourceIsOther_WellFormed() {
        channelsState.joined += ChannelState("#channel", users = generateUsers("test-nick", "someone-else"))

        handler.handle(PartMessage(source = Prefix(nick = "someone-else"), channels = listOf("#channel")))

        assertEquals(channelsStateWith(listOf(ChannelState(name = "#channel", users = generateUsers("test-nick"))), caseMappingState), channelsState)
    }

    @Test fun test_handle_SourceIsOther_NotInChannel() {
        handler.handle(PartMessage(source = Prefix(nick = "someone-else"), channels = listOf("#channel")))

        assertEquals(emptyChannelsState(caseMappingState), channelsState)
    }

}