package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.irc.message.rfc1459.NickMessage
import engineer.carrot.warren.kale.irc.message.utility.CaseMapping
import engineer.carrot.warren.kale.irc.prefix.Prefix
import engineer.carrot.warren.warren.state.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class NickHandlerTests {

    lateinit var handler: NickHandler
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
        handler = NickHandler(connectionState, channelsState.joined)
    }

    @Test fun test_handle_FromIsNull_DoesNothing() {
        channelsState.joined += ChannelState(name = "#channel", users = generateUsers("someone", "someone-else"))
        channelsState.joined += ChannelState(name = "#channel2", users = generateUsers("another-person", "someone-else"))

        handler.handle(NickMessage(nickname = "someone-else-2"))

        val expectedChannelOneState = ChannelState(name = "#channel", users = generateUsers("someone", "someone-else"))
        val expectedChannelTwoState = ChannelState(name = "#channel2", users = generateUsers("another-person", "someone-else"))
        val expectedChannelsState = channelsStateWith(listOf(expectedChannelOneState, expectedChannelTwoState), caseMappingState)

        assertEquals(channelsState, expectedChannelsState)
    }

    @Test fun test_handle_FromIsSelf_RenamesSelf() {
        channelsState.joined += ChannelState(name = "#channel", users = generateUsers("test-nick", "someone-else"))
        channelsState.joined += ChannelState(name = "#channel2", users = generateUsers("another-person", "someone-else"))

        handler.handle(NickMessage(source = Prefix(nick = "test-nick", host = "somewhere"), nickname = "test-new-nick"))

        val expectedChannelOneState = ChannelState(name = "#channel", users = generateUsers("test-new-nick", "someone-else"))
        val expectedChannelTwoState = ChannelState(name = "#channel2", users = generateUsers("another-person", "someone-else"))
        val expectedChannelsState = channelsStateWith(listOf(expectedChannelOneState, expectedChannelTwoState), caseMappingState)

        assertEquals(channelsState, expectedChannelsState)
    }

    @Test fun test_handle_FromIsSomeoneElse_UserIsRenamedInAllChannels() {
        channelsState.joined += ChannelState(name = "#channel", users = generateUsers("someone", "someone-else"))
        channelsState.joined += ChannelState(name = "#channel2", users = generateUsers("another-person", "someone-else"))

        handler.handle(NickMessage(source = Prefix(nick = "someone-else", host = "somewhere"), nickname = "someone-else-2"))

        val expectedChannelOneState = ChannelState(name = "#channel", users = generateUsers("someone", "someone-else-2"))
        val expectedChannelTwoState = ChannelState(name = "#channel2", users = generateUsers("another-person", "someone-else-2"))
        val expectedChannelsState = channelsStateWith(listOf(expectedChannelOneState, expectedChannelTwoState), caseMappingState)

        assertEquals(channelsState, expectedChannelsState)
    }

}