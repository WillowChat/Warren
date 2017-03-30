package chat.willow.warren.handler.rpl.isupport

import chat.willow.kale.helper.CaseMapping
import chat.willow.kale.irc.message.rfc1459.rpl.Rpl005Message
import chat.willow.kale.irc.tag.TagStore
import chat.willow.warren.extension.cap.ICapManager
import chat.willow.warren.extension.monitor.MonitorState
import chat.willow.warren.state.*
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test

class Rpl005HandlerTests {
    lateinit var handler: Rpl005Handler
    lateinit var state: ParsingState
    lateinit var userPrefixesState: UserPrefixesState
    lateinit var channelModesState: ChannelModesState
    lateinit var channelTypesState: ChannelTypesState
    lateinit var caseMappingState: CaseMappingState
    lateinit var monitorState: MonitorState
    lateinit var mockCapManager: ICapManager
    lateinit var mockPrefixHandler: IRpl005PrefixHandler
    lateinit var mockChannelModesHandler: IRpl005ChanModesHandler
    lateinit var mockChannelTypesHandler: IRpl005ChanTypesHandler
    lateinit var mockCaseMappingHandler: IRpl005CaseMappingHandler
    lateinit var mockMonitorHandler: IRpl005MonitorHandler

    @Before fun setUp() {
        userPrefixesState = UserPrefixesState(prefixesToModes = mapOf('@' to 'o'))
        channelModesState = ChannelModesState(typeA = setOf('a'), typeB = setOf('b'), typeC = setOf('c'), typeD = setOf('d'))
        channelTypesState = ChannelTypesState(types = setOf('#'))
        caseMappingState = CaseMappingState(mapping = CaseMapping.RFC1459)
        monitorState = MonitorState(maxCount = 0)
        state = ParsingState(userPrefixesState, channelModes = channelModesState, channelTypes = channelTypesState, caseMapping = caseMappingState)
        mockPrefixHandler = mock()
        mockCapManager = mock()
        mockChannelModesHandler = mock()
        mockChannelTypesHandler = mock()
        mockCaseMappingHandler = mock()
        mockMonitorHandler = mock()
        handler = Rpl005Handler(state, monitorState, mockPrefixHandler, mockChannelModesHandler, mockChannelTypesHandler, mockCaseMappingHandler, mockMonitorHandler)
    }

    @Test fun test_handle_UserPrefixes() {
        handler.handle(Rpl005Message.Message(source = "test.server", target = "test_user", tokens = mapOf("PREFIX" to "(ovh)@+%")), TagStore())

        verify(mockPrefixHandler).handle("(ovh)@+%", userPrefixesState)
    }

    @Test fun test_handle_ChannelModes() {
        handler.handle(Rpl005Message.Message(source = "test.server", target = "test_user", tokens = mapOf("CHANMODES" to "eIb,k,l,imnpstSr")), TagStore())

        verify(mockChannelModesHandler).handle("eIb,k,l,imnpstSr", channelModesState)
    }

    @Test fun test_handle_CaseMapping() {
        handler.handle(Rpl005Message.Message(source = "test.server", target = "test_user", tokens = mapOf("CASEMAPPING" to "something")), TagStore())

        verify(mockCaseMappingHandler).handle("something", caseMappingState)
    }

    @Test fun test_handle_Monitor() {
        handler.handle(Rpl005Message.Message(source = "test.server", target = "test_user", tokens = mapOf("MONITOR" to "1234")), TagStore())

        verify(mockMonitorHandler).handle("1234", monitorState)
    }

}