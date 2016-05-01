package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.irc.message.rfc1459.KickMessage
import engineer.carrot.warren.warren.state.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class KickHandlerTests {

    lateinit var handler: KickHandler
    lateinit var connectionState: ConnectionState
    lateinit var channelsState: ChannelsState

    @Before fun setUp() {
        val lifecycleState = LifecycleState.DISCONNECTED
        val capLifecycleState = CapLifecycle.NEGOTIATED
        val capState = CapState(lifecycle = capLifecycleState, negotiate = setOf(), server = mapOf(), accepted = setOf(), rejected = setOf())
        val saslState = SaslState(shouldAuth = false, lifecycle = SaslLifecycle.AUTH_FAILED, credentials = null)
        connectionState = ConnectionState(server = "test.server", port = 6697, nickname = "test-nick", username = "test-nick", lifecycle = lifecycleState, cap = capState, sasl = saslState)
        channelsState = ChannelsState(joined = mutableMapOf())
        handler = KickHandler(connectionState, channelsState)
    }

    @Test fun test_handle_SingleNick_MultipleUsers_RemovesUserFromChannels() {
        channelsState.joined["#channel"] = ChannelState(name = "#channel", users = generateUsers("someone", "someone-else"))
        channelsState.joined["#channel2"] = ChannelState(name = "#channel2", users = generateUsers("another-person", "someone-else"))

        handler.handle(KickMessage(users = listOf("someone"), channels = listOf("#channel", "#channel2")))

        val expectedChannelOneState = ChannelState(name = "#channel", users = generateUsers("someone-else"))
        val expectedChannelTwoState = ChannelState(name = "#channel2", users = generateUsers("another-person", "someone-else"))
        val expectedChannelsState = ChannelsState(joined = mutableMapOf("#channel" to expectedChannelOneState, "#channel2" to expectedChannelTwoState))

        assertEquals(channelsState, expectedChannelsState)
    }

    @Test fun test_handle_MultipleNicks_NotSelf_RemovesFromChannel() {
        channelsState.joined["#channel"] = ChannelState(name = "#channel", users = generateUsers("someone", "someone-else", "last-person"))

        handler.handle(KickMessage(users = listOf("someone", "someone-else"), channels = listOf("#channel")))

        val expectedChannelOneState = ChannelState(name = "#channel", users = generateUsers("last-person"))
        val expectedChannelsState = ChannelsState(joined = mutableMapOf("#channel" to expectedChannelOneState))

        assertEquals(channelsState, expectedChannelsState)
    }

    @Test fun test_handle_UserNotInChannel_DoesNothing() {
        channelsState.joined["#channel"] = ChannelState(name = "#channel", users = generateUsers("someone"))

        handler.handle(KickMessage(users = listOf("nonexistent-user"), channels = listOf("#channel")))

        val expectedChannelOneState = ChannelState(name = "#channel", users = generateUsers("someone"))
        val expectedChannelsState = ChannelsState(joined = mutableMapOf("#channel" to expectedChannelOneState))

        assertEquals(channelsState, expectedChannelsState)
    }

    @Test fun test_handle_KickSelf_LeavesChannel() {
        channelsState.joined["#channel"] = ChannelState(name = "#channel", users = generateUsers("test-nick"))

        handler.handle(KickMessage(users = listOf("test-nick"), channels = listOf("#channel")))

        val expectedChannelsState = ChannelsState(joined = mutableMapOf())

        assertEquals(channelsState, expectedChannelsState)
    }

}