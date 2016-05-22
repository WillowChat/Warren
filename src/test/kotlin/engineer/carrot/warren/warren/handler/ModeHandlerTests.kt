package engineer.carrot.warren.warren.handler

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import engineer.carrot.warren.kale.irc.message.rfc1459.ModeMessage
import engineer.carrot.warren.kale.irc.message.utility.CaseMapping
import engineer.carrot.warren.kale.irc.prefix.Prefix
import engineer.carrot.warren.warren.ChannelModeEvent
import engineer.carrot.warren.warren.IWarrenEventDispatcher
import engineer.carrot.warren.warren.UserModeEvent
import engineer.carrot.warren.warren.state.*
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals

class ModeHandlerTests {

    lateinit var handler: ModeHandler
    lateinit var mockEventDispatcher: IWarrenEventDispatcher
    lateinit var channelsState: ChannelsState
    val caseMappingState = CaseMappingState(mapping = CaseMapping.RFC1459)

    @Before fun setUp() {
        mockEventDispatcher = mock()
        val channelTypes = ChannelTypesState(types = setOf('#'))
        channelsState = emptyChannelsState(caseMappingState)
        val userPrefixesState = UserPrefixesState(prefixesToModes = mapOf('+' to 'v', '@' to 'o'))
        handler = ModeHandler(mockEventDispatcher, channelTypes, channelsState.joined, userPrefixesState, caseMappingState)
    }

    @Test fun test_handle_ChannelModeChange_NoPrefix_FiresEvents() {
        val firstExpectedModifier = ModeMessage.ModeModifier(type = '+', mode = 'x', parameter = "someone")
        val secondExpectedModifier = ModeMessage.ModeModifier(type = '+', mode = 'y')

        handler.handle(ModeMessage(source = null, target = "#channel", modifiers = listOf(firstExpectedModifier, secondExpectedModifier)))

        verify(mockEventDispatcher).fire(ChannelModeEvent(user = null, channel = "#channel", modifier = firstExpectedModifier))
        verify(mockEventDispatcher).fire(ChannelModeEvent(user = null, channel = "#channel", modifier = secondExpectedModifier))
    }

    @Test fun test_handle_ChannelModeChange_WithPrefix_FiresEvents() {
        val firstExpectedModifier = ModeMessage.ModeModifier(type = '+', mode = 'x', parameter = "someone")
        val secondExpectedModifier = ModeMessage.ModeModifier(type = '+', mode = 'y')

        handler.handle(ModeMessage(source = Prefix(nick = "admin"), target = "#channel", modifiers = listOf(firstExpectedModifier, secondExpectedModifier)))

        verify(mockEventDispatcher).fire(ChannelModeEvent(user = Prefix(nick = "admin"), channel = "#channel", modifier = firstExpectedModifier))
        verify(mockEventDispatcher).fire(ChannelModeEvent(user = Prefix(nick = "admin"), channel = "#channel", modifier = secondExpectedModifier))
    }

    @Test fun test_handle_ChannelModeChange_UserPrefixAdded() {
        channelsState.joined += ChannelState("#channel", generateUsersWithModes(("someone" to mutableSetOf()), mappingState = caseMappingState))

        val addVoiceModifier = ModeMessage.ModeModifier(type = '+', mode = 'v', parameter = "someone")

        handler.handle(ModeMessage(target = "#channel", modifiers = listOf(addVoiceModifier)))

        assertEquals(mutableSetOf('v'), channelsState.joined["#channel"]!!.users["someone"]!!.modes)
    }

    @Test fun test_handle_ChannelModeChange_UserPrefixRemoved() {
        channelsState.joined += ChannelState("#channel", generateUsersWithModes(("someone" to mutableSetOf('o')), mappingState = caseMappingState))

        val addVoiceModifier = ModeMessage.ModeModifier(type = '-', mode = 'o', parameter = "someone")

        handler.handle(ModeMessage(target = "#channel", modifiers = listOf(addVoiceModifier)))

        assertEquals(mutableSetOf<Char>(), channelsState.joined["#channel"]!!.users["someone"]!!.modes)
    }

    @Test fun test_handle_ChannelModeChange_UserPrefixForNonExistentChannel_NothingHappens() {
        channelsState.joined += ChannelState("#channel", generateUsersWithModes(("someone" to mutableSetOf('o')), mappingState = caseMappingState))

        val addVoiceModifier = ModeMessage.ModeModifier(type = '-', mode = 'o', parameter = "someone")

        handler.handle(ModeMessage(target = "#anotherchannel", modifiers = listOf(addVoiceModifier)))

        assertEquals(mutableSetOf('o'), channelsState.joined["#channel"]!!.users["someone"]!!.modes)
    }

    @Test fun test_handle_ChannelModeChange_UserPrefixForNonExistentUser_NothingHappens() {
        channelsState.joined += ChannelState("#channel", generateUsersWithModes(("someone" to mutableSetOf('o')), mappingState = caseMappingState))

        val addVoiceModifier = ModeMessage.ModeModifier(type = '-', mode = 'o', parameter = "someone-else")

        handler.handle(ModeMessage(target = "#channel", modifiers = listOf(addVoiceModifier)))

        assertEquals(mutableSetOf('o'), channelsState.joined["#channel"]!!.users["someone"]!!.modes)
    }

    @Test fun test_handle_UserModeChange_FiresEvents() {
        val firstExpectedModifier = ModeMessage.ModeModifier(type = '+', mode = 'v', parameter = "someone")
        val secondExpectedModifier = ModeMessage.ModeModifier(type = '+', mode = 'x')

        handler.handle(ModeMessage(source = null, target = "someone", modifiers = listOf(firstExpectedModifier, secondExpectedModifier)))

        verify(mockEventDispatcher).fire(UserModeEvent(user = "someone", modifier = firstExpectedModifier))
        verify(mockEventDispatcher).fire(UserModeEvent(user = "someone", modifier = secondExpectedModifier))
    }

}