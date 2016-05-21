package engineer.carrot.warren.warren.handler

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import engineer.carrot.warren.kale.irc.CharacterCodes
import engineer.carrot.warren.kale.irc.message.rfc1459.PrivMsgMessage
import engineer.carrot.warren.kale.irc.prefix.Prefix
import engineer.carrot.warren.warren.*
import engineer.carrot.warren.warren.state.ChannelTypesState
import org.junit.Before
import org.junit.Test

class PrivMsgHandlerTests {

    lateinit var handler: PrivMsgHandler
    lateinit var channelsState: ChannelTypesState
    lateinit var mockEventDispatcher: IWarrenEventDispatcher

    @Before fun setUp() {
        channelsState = ChannelTypesState(types = setOf('#', '&'))
        mockEventDispatcher = mock()
        handler = PrivMsgHandler(mockEventDispatcher, channelsState)
    }

    @Test fun test_handle_ChannelMessage_FiresEvent() {
        handler.handle(PrivMsgMessage(source = Prefix(nick = "someone"), target = "&channel", message = "a test message"))

        verify(mockEventDispatcher).fire(ChannelMessageEvent(user = Prefix(nick = "someone"), channel = "&channel", message = "a test message"))
    }

    @Test fun test_handle_PrivateMessage_FiresEvent() {
        handler.handle(PrivMsgMessage(source = Prefix(nick = "someone"), target = "not-a-channel", message = "a test message"))

        verify(mockEventDispatcher).fire(PrivateMessageEvent(user = Prefix(nick = "someone"), message = "a test message"))
    }

    @Test fun test_handle_NoSource_DoesNothing() {
        handler.handle(PrivMsgMessage(source = null, target = "not-a-channel", message = "a test message"))

        verify(mockEventDispatcher, never()).fire(any<IWarrenEvent>())
    }

    @Test fun test_handle_ChannelMessage_Action_FiresEvent() {
        handler.handle(PrivMsgMessage(source = Prefix(nick = "someone"), target = "&channel", message = "${CharacterCodes.CTCP}ACTION an action${CharacterCodes.CTCP}"))

        verify(mockEventDispatcher).fire(ChannelActionEvent(user = Prefix(nick = "someone"), channel = "&channel", message = "an action"))
    }

    @Test fun test_handle_PrivateMessage_Action_FiresEvent() {
        handler.handle(PrivMsgMessage(source = Prefix(nick = "someone"), target = "not a channel", message = "${CharacterCodes.CTCP}ACTION an action${CharacterCodes.CTCP}"))

        verify(mockEventDispatcher).fire(PrivateActionEvent(user = Prefix(nick = "someone"), message = "an action"))
    }

}