package engineer.carrot.warren.warren.handler.rpl.Rpl005

import engineer.carrot.warren.kale.irc.message.utility.CaseMapping
import engineer.carrot.warren.warren.state.CaseMappingState
import engineer.carrot.warren.warren.state.ChannelTypesState
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class Rpl005CaseMappingHandlerTests {

    lateinit var handler: IRpl005CaseMappingHandler
    lateinit var caseMappingState: CaseMappingState
    val caseMapping = CaseMapping.RFC1459

    @Before fun setUp() {
        handler = Rpl005CaseMappingHandler
        caseMappingState = CaseMappingState(caseMapping)
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

}
