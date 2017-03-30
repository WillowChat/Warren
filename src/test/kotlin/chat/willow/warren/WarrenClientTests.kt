package chat.willow.warren

import chat.willow.kale.irc.CharacterCodes
import chat.willow.kale.irc.message.rfc1459.*
import chat.willow.kale.irc.prefix.Prefix
import chat.willow.warren.event.IWarrenEventDispatcher
import chat.willow.warren.state.ChannelState
import chat.willow.warren.state.ChannelUserState
import chat.willow.warren.state.generateUsersFromNicks
import com.nhaarman.mockito_kotlin.*
import org.junit.Before
import org.junit.Test

class WarrenClientTests {

    private lateinit var sut: WarrenClient
    private lateinit var mockConnection: IIrcConnection
    private lateinit var mockEvents: IWarrenEventDispatcher

    @Before fun setUp() {
        mockConnection = mock()
        mockEvents = mock()

        sut = WarrenClient(mockConnection, mockEvents)
    }

    @Test fun test_start_TellsConnectionToStart() {
        sut.start()

        verify(mockConnection, only()).start()
    }

    @Test fun test_join_ChannelOnly_SendsJoinMessageWithoutKey() {
        sut.join("#channel")

        verify(mockConnection, only()).send(JoinMessage.Command(channels = listOf("#channel")))
    }

    @Test fun test_join_ChannelAndKey_SendsJoinMessageWithKey() {
        sut.join("#channel", "key")

        verify(mockConnection, only()).send(JoinMessage.Command(channels = listOf("#channel"), keys = listOf("key")))
    }

    @Test fun test_leave_SendsPartMessage() {
        sut.leave("#channel")

        verify(mockConnection, only()).send(PartMessage.Command(channels = listOf("#channel")))
    }

    @Test fun test_builder_SanityCheck() {
        val mockFactory: IWarrenFactory = mock()
        val mockConnection: IIrcConnection = mock()
        whenever(mockFactory.create(any(), any(), any(), any(), any(), any())).thenReturn(mockConnection)

        val client = WarrenClient.build(mockFactory) {
            server("test.server")
            user("testnick")
            channel("#testchannel")
            channel("#testchannel2" to "testkey")
        }

        client.start()

        verify(mockConnection).start()
    }

}

class WarrenChannelTests {

    private lateinit var sut: WarrenChannel
    private lateinit var mockClient: IClientMessageSending
    private lateinit var state: ChannelState

    @Before fun setUp() {
        val users = generateUsersFromNicks(listOf("user1", "user2"))
        state = ChannelState(name = "#channel", users = users)
        mockClient = mock()

        sut = WarrenChannel(state, mockClient)
    }

    @Test fun test_send_Message_TellsClientToSendMessage_ToChannelName() {
        sut.send("test message")

        verify(mockClient, only()).send("test message", target = "#channel")
    }

    @Test fun test_addMode_SendsModeMessage() {
        sut.addMode('a', user = "user1")

        val modeModifier = ModeMessage.ModeModifier(type = CharacterCodes.PLUS, mode = 'a', parameter = "user1")
        verify(mockClient, only()).send(ModeMessage.Command(target = "#channel", modifiers = listOf(modeModifier)))
    }

    @Test fun test_removeMode_SendsModeMessage() {
        sut.removeMode('a', user = "user1")

        val modeModifier = ModeMessage.ModeModifier(type = CharacterCodes.MINUS, mode = 'a', parameter = "user1")
        verify(mockClient, only()).send(ModeMessage.Command(target = "#channel", modifiers = listOf(modeModifier)))
    }

    @Test fun test_kick_SendsKickMessage() {
        sut.kick("user1")

        verify(mockClient, only()).send(KickMessage.Command(users = listOf("user1"), channels = listOf("#channel")))
    }

    @Test fun test_invite_SendsInviteMessage() {
        sut.invite("user1")

        verify(mockClient, only()).send(InviteMessage.Command(user = "user1", channel = "#channel"))
    }

}

class WarrenChannelUserTests {

    private lateinit var sut: WarrenChannelUser
    private lateinit var mockChannel: IWarrenChannel
    private lateinit var state: ChannelUserState

    @Before fun setUp() {
        state = ChannelUserState(prefix = Prefix(nick = "user1"), modes = mutableSetOf('o', 'v'))
        mockChannel = mock()

        sut = WarrenChannelUser(state, mockChannel)
    }

    @Test fun test_send_TellsChannelToSend_TargetedMessage() {
        sut.send("test message")

        verify(mockChannel, only()).send("user1: test message")
    }

    @Test fun test_kick_TellsChannelToKickUser() {
        sut.kick()

        verify(mockChannel).kick("user1")
    }

}