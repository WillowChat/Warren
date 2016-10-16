package engineer.carrot.warren.warren.handler.rpl.Rpl005

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import engineer.carrot.warren.kale.irc.message.rfc1459.rpl.Rpl005Message
import engineer.carrot.warren.kale.irc.message.utility.CaseMapping
import engineer.carrot.warren.warren.state.*
import org.junit.Before
import org.junit.Test

class Rpl005HandlerTests {
    lateinit var handler: Rpl005Handler
    lateinit var state: ParsingState
    lateinit var userPrefixesState: UserPrefixesState
    lateinit var channelModesState: ChannelModesState
    lateinit var channelTypesState: ChannelTypesState
    lateinit var caseMappingState: CaseMappingState
    lateinit var prefixHandler: IRpl005PrefixHandler
    lateinit var channelModesHandler: IRpl005ChanModesHandler
    lateinit var channelTypesHandler: IRpl005ChanTypesHandler
    lateinit var caseMappingHandler: IRpl005CaseMappingHandler

    @Before fun setUp() {
        userPrefixesState = UserPrefixesState(prefixesToModes = mapOf('@' to 'o'))
        channelModesState = ChannelModesState(typeA = setOf('a'), typeB = setOf('b'), typeC = setOf('c'), typeD = setOf('d'))
        channelTypesState = ChannelTypesState(types = setOf('#'))
        caseMappingState = CaseMappingState(mapping = CaseMapping.RFC1459)
        state = ParsingState(userPrefixesState, channelModes = channelModesState, channelTypes = channelTypesState, caseMapping = caseMappingState)
        prefixHandler = mock()
        channelModesHandler = mock()
        channelTypesHandler = mock()
        caseMappingHandler = mock()
        handler = Rpl005Handler(state, prefixHandler, channelModesHandler, channelTypesHandler, caseMappingHandler)
    }

    @Test fun test_handle_UserPrefixes() {
        handler.handle(Rpl005Message(source = "test.server", target = "test_user", tokens = mapOf("PREFIX" to "(ovh)@+%")), mapOf())

        verify(prefixHandler).handle("(ovh)@+%", userPrefixesState)
    }

    @Test fun test_handle_ChannelModes() {
        handler.handle(Rpl005Message(source = "test.server", target = "test_user", tokens = mapOf("CHANMODES" to "eIb,k,l,imnpstSr")), mapOf())

        verify(channelModesHandler).handle("eIb,k,l,imnpstSr", channelModesState)
    }

    @Test fun test_handle_CaseMapping() {
        handler.handle(Rpl005Message(source = "test.server", target = "test_user", tokens = mapOf("CASEMAPPING" to "something")), mapOf())

        verify(caseMappingHandler).handle("something", caseMappingState)
    }

}