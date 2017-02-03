package chat.willow.warren.handler.rpl.isupport

import chat.willow.kale.irc.message.utility.CaseMapping
import chat.willow.warren.state.CaseMappingState
import chat.willow.warren.state.ChannelUsersState
import chat.willow.warren.state.JoinedChannelsState
import chat.willow.warren.state.JoiningChannelsState
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class Rpl005CaseMappingHandlerTests {

    lateinit var handler: IRpl005CaseMappingHandler
    lateinit var caseMappingState: CaseMappingState
    lateinit var joinedChannelsState: JoinedChannelsState
    lateinit var joiningChannelsState: JoiningChannelsState
    lateinit var channelUsersState: ChannelUsersState
    val caseMapping = CaseMapping.RFC1459

    @Before fun setUp() {
        handler = Rpl005CaseMappingHandler
        caseMappingState = CaseMappingState(caseMapping)
        joinedChannelsState = JoinedChannelsState(caseMappingState)
        joiningChannelsState = JoiningChannelsState(caseMappingState)
        channelUsersState = ChannelUsersState(caseMappingState)
    }

    @Test fun test_handle_RFC1459() {
        handler.handle("rfc1459", caseMappingState)

        assertEquals(CaseMappingState(mapping = CaseMapping.RFC1459), caseMappingState)
    }

    @Test fun test_handle_STRICT_RFC1459() {
        handler.handle("strict-rfc1459", caseMappingState)

        assertEquals(CaseMappingState(mapping = CaseMapping.STRICT_RFC1459), caseMappingState)
    }

    @Test fun test_handle_ASCII() {
        handler.handle("ascii", caseMappingState)

        assertEquals(CaseMappingState(mapping = CaseMapping.ASCII), caseMappingState)
    }

    @Test fun test_handle_Other_DefaultsToRFC1459() {
        handler.handle("something else", caseMappingState)

        assertEquals(CaseMappingState(mapping = CaseMapping.RFC1459), caseMappingState)
    }

    @Test fun test_handle_ChannelsStateCaseMappingIsAlsoUpdated() {
        handler.handle("ascii", caseMappingState)

        assertEquals(CaseMappingState(mapping = CaseMapping.ASCII), joinedChannelsState.mappingState)
        assertEquals(CaseMappingState(mapping = CaseMapping.ASCII), joiningChannelsState.mappingState)
        assertEquals(CaseMappingState(mapping = CaseMapping.ASCII), channelUsersState.mappingState)
    }

}
