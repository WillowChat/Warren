package engineer.carrot.warren.warren.handler

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import engineer.carrot.warren.kale.irc.message.rfc1459.ModeMessage
import engineer.carrot.warren.kale.irc.message.rfc1459.PongMessage
import engineer.carrot.warren.kale.irc.prefix.Prefix
import engineer.carrot.warren.warren.*
import engineer.carrot.warren.warren.state.ChannelTypesState
import org.junit.Before
import org.junit.Test

class ModeHandlerTests {

    lateinit var handler: ModeHandler
    lateinit var mockEventDispatcher: IWarrenEventDispatcher

    @Before fun setUp() {
        mockEventDispatcher = mock()
        val channelTypes = ChannelTypesState(types = setOf('#'))
        handler = ModeHandler(mockEventDispatcher, channelTypes)
    }

    @Test fun test_handle_ChannelModeChange_NoPrefix_FiresEvents() {
        val firstExpectedModifier = ModeMessage.ModeModifier(type = '+', mode = 'v', parameter = "someone")
        val secondExpectedModifier = ModeMessage.ModeModifier(type = '+', mode = 'x')

        handler.handle(ModeMessage(source = null, target = "#channel", modifiers = listOf(firstExpectedModifier, secondExpectedModifier)))

        verify(mockEventDispatcher).fire(ChannelModeEvent(user = null, channel = "#channel", modifier = firstExpectedModifier))
        verify(mockEventDispatcher).fire(ChannelModeEvent(user = null, channel = "#channel", modifier = secondExpectedModifier))
    }

    @Test fun test_handle_ChannelModeChange_WithPrefix_FiresEvents() {
        val firstExpectedModifier = ModeMessage.ModeModifier(type = '+', mode = 'v', parameter = "someone")
        val secondExpectedModifier = ModeMessage.ModeModifier(type = '+', mode = 'x')

        handler.handle(ModeMessage(source = Prefix(nick = "admin"), target = "#channel", modifiers = listOf(firstExpectedModifier, secondExpectedModifier)))

        verify(mockEventDispatcher).fire(ChannelModeEvent(user = Prefix(nick = "admin"), channel = "#channel", modifier = firstExpectedModifier))
        verify(mockEventDispatcher).fire(ChannelModeEvent(user = Prefix(nick = "admin"), channel = "#channel", modifier = secondExpectedModifier))
    }

    @Test fun test_handle_UserModeChange_FiresEvents() {
        val firstExpectedModifier = ModeMessage.ModeModifier(type = '+', mode = 'v', parameter = "someone")
        val secondExpectedModifier = ModeMessage.ModeModifier(type = '+', mode = 'x')

        handler.handle(ModeMessage(source = null, target = "someone", modifiers = listOf(firstExpectedModifier, secondExpectedModifier)))

        verify(mockEventDispatcher).fire(UserModeEvent(user = "someone", modifier = firstExpectedModifier))
        verify(mockEventDispatcher).fire(UserModeEvent(user = "someone", modifier = secondExpectedModifier))
    }

}