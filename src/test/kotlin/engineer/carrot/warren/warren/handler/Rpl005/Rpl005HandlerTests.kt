package engineer.carrot.warren.warren.handler.Rpl005

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import engineer.carrot.warren.kale.irc.message.rpl.Rpl005Message
import engineer.carrot.warren.warren.state.ChannelModesState
import engineer.carrot.warren.warren.state.ChannelPrefixesState
import engineer.carrot.warren.warren.state.ParsingState
import engineer.carrot.warren.warren.state.UserPrefixesState
import org.junit.Before
import org.junit.Test

class Rpl005HandlerTests {
    lateinit var handler: Rpl005Handler
    lateinit var state: ParsingState
    lateinit var userPrefixesState: UserPrefixesState
    lateinit var channelModesState: ChannelModesState
    lateinit var channelPrefixesState: ChannelPrefixesState
    lateinit var prefixHandler: IRpl005PrefixHandler
    lateinit var channelModesHandler: IRpl005ChanModesHandler

    @Before fun setUp() {
        userPrefixesState = UserPrefixesState(prefixesToModes = mapOf('@' to 'o'))
        channelModesState = ChannelModesState(typeA = setOf('a'), typeB = setOf('b'), typeC = setOf('c'), typeD = setOf('d'))
        channelPrefixesState = ChannelPrefixesState(prefixes = setOf('#'))
        state = ParsingState(userPrefixesState, channelModes = channelModesState, channelPrefixes = channelPrefixesState)
        prefixHandler = mock()
        channelModesHandler = mock()
        handler = Rpl005Handler(state, prefixHandler, channelModesHandler)
    }

    @Test fun test_handle_UserPrefixes() {
        handler.handle(Rpl005Message(source = "test.server", target = "test_user", tokens = mapOf("PREFIX" to "(ovh)@+%")))

        verify(prefixHandler).handle("(ovh)@+%", userPrefixesState)
    }

    @Test fun test_handle_ChannelModes() {
        handler.handle(Rpl005Message(source = "test.server", target = "test_user", tokens = mapOf("CHANMODES" to "eIb,k,l,imnpstSr")))

        verify(channelModesHandler).handle("eIb,k,l,imnpstSr", channelModesState)
    }

}