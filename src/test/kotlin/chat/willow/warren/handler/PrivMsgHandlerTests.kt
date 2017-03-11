package chat.willow.warren.handler

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import chat.willow.kale.irc.CharacterCodes
import chat.willow.kale.irc.message.rfc1459.PrivMsgMessage
import chat.willow.kale.irc.message.utility.CaseMapping
import chat.willow.kale.irc.prefix.Prefix
import chat.willow.kale.irc.tag.TagStore
import chat.willow.warren.IClientMessageSending
import chat.willow.warren.WarrenChannel
import chat.willow.warren.WarrenChannelUser
import chat.willow.warren.event.*
import chat.willow.warren.state.*
import org.junit.Before
import org.junit.Test

class PrivMsgHandlerTests {

    lateinit var handler: PrivMsgHandler
    lateinit var channelTypesState: ChannelTypesState
    lateinit var joinedChannelsState: JoinedChannelsState
    lateinit var mockEventDispatcher: IWarrenEventDispatcher
    lateinit var mockClientMessageSending: IClientMessageSending

    @Before fun setUp() {
        joinedChannelsState = JoinedChannelsState(mappingState = CaseMappingState(CaseMapping.RFC1459))
        channelTypesState = ChannelTypesState(types = setOf('#', '&'))
        mockEventDispatcher = mock()
        mockClientMessageSending = mock()

        handler = PrivMsgHandler(mockEventDispatcher, mockClientMessageSending, joinedChannelsState, channelTypesState)
    }

    @Test fun test_handle_ChannelMessage_FiresEvent() {
        val channelState = emptyChannel("&channel")
        val userState = generateUser("someone")
        channelState.users += userState

        joinedChannelsState += channelState

        handler.handle(PrivMsgMessage(source = Prefix(nick = "someone"), target = "&channel", message = "a test message"), TagStore())

        val channel = WarrenChannel(state = channelState, client = mockClientMessageSending)
        val user = WarrenChannelUser(state = userState, channel = channel)
        verify(mockEventDispatcher).fire(ChannelMessageEvent(user = user, channel = channel, message = "a test message"))
    }

    @Test fun test_handle_ChannelMessage_NotInChannel_DoesNothing() {
        val channel = emptyChannel("&channel")

        joinedChannelsState += channel
        handler.handle(PrivMsgMessage(source = Prefix(nick = "someone"), target = "&notInChannel", message = "a test message"), TagStore())

        verify(mockEventDispatcher, never()).fire(any<IWarrenEvent>())
    }

    @Test fun test_handle_ChannelMessage_MissingUser_DoesNothing() {
        handler.handle(PrivMsgMessage(source = Prefix(nick = "someone"), target = "&notInChannel", message = "a test message"), TagStore())

        verify(mockEventDispatcher, never()).fire(any<IWarrenEvent>())
    }

    @Test fun test_handle_ChannelMessage_UserNotInChannel_StillFiresEvent() {
        val channelState = emptyChannel("&channel")
        val userState = generateUser("someone")
        channelState.users += userState

        joinedChannelsState += channelState

        handler.handle(PrivMsgMessage(source = Prefix(nick = "someone-else"), target = "&channel", message = "a test message"), TagStore())

        val channel = WarrenChannel(state = channelState, client = mockClientMessageSending)
        val user = WarrenChannelUser(state = generateUser("someone-else"), channel = channel)
        verify(mockEventDispatcher).fire(ChannelMessageEvent(user = user, channel = channel, message = "a test message"))
    }

    @Test fun test_handle_PrivateMessage_FiresEvent() {
        handler.handle(PrivMsgMessage(source = Prefix(nick = "someone"), target = "not-a-channel", message = "a test message"), TagStore())

        verify(mockEventDispatcher).fire(PrivateMessageEvent(user = Prefix(nick = "someone"), message = "a test message"))
    }

    @Test fun test_handle_NoSource_DoesNothing() {
        handler.handle(PrivMsgMessage(source = null, target = "not-a-channel", message = "a test message"), TagStore())

        verify(mockEventDispatcher, never()).fire(any<IWarrenEvent>())
    }

    @Test fun test_handle_ChannelMessage_Action_FiresEvent() {
        val channelState = emptyChannel("&channel")
        val userState = generateUser("someone")
        channelState.users += userState

        joinedChannelsState += channelState

        handler.handle(PrivMsgMessage(source = Prefix(nick = "someone"), target = "&channel", message = "${CharacterCodes.CTCP}ACTION an action${CharacterCodes.CTCP}"), TagStore())

        val channel = WarrenChannel(state = channelState, client = mockClientMessageSending)
        val user = WarrenChannelUser(state = userState, channel = channel)
        verify(mockEventDispatcher).fire(ChannelActionEvent(user = user, channel = channel, message = "an action"))
    }

    @Test fun test_handle_ChannelMessage_UnknownCtcp_DoesNotFireEvent() {
        val channel = emptyChannel("&channel")
        channel.users += generateUser("someone")

        joinedChannelsState += channel

        handler.handle(PrivMsgMessage(source = Prefix(nick = "someone"), target = "&channel", message = "${CharacterCodes.CTCP}UNKNOWN ${CharacterCodes.CTCP}"), TagStore())

        verify(mockEventDispatcher, never()).fire(any<IWarrenEvent>())
    }

    @Test fun test_handle_PrivateMessage_Action_FiresEvent() {
        handler.handle(PrivMsgMessage(source = Prefix(nick = "someone"), target = "not a channel", message = "${CharacterCodes.CTCP}ACTION an action${CharacterCodes.CTCP}"), TagStore())

        verify(mockEventDispatcher).fire(PrivateActionEvent(user = Prefix(nick = "someone"), message = "an action"))
    }

    @Test fun test_handle_PrivateMessage_UnknownCtcp_DoesNotFireEvent() {
        handler.handle(PrivMsgMessage(source = Prefix(nick = "someone"), target = "not a channel", message = "${CharacterCodes.CTCP}UNKNOWN ${CharacterCodes.CTCP}"), TagStore())

        verify(mockEventDispatcher, never()).fire(any<IWarrenEvent>())
    }

}