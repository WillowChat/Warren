package engineer.carrot.warren.warren.handler.rpl

import engineer.carrot.warren.kale.irc.message.rpl.Rpl471Message
import engineer.carrot.warren.kale.irc.message.utility.CaseMapping
import engineer.carrot.warren.warren.state.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class Rpl471HandlerTests {

    lateinit var handler: Rpl471Handler
    lateinit var channelsState: ChannelsState
    val caseMappingState = CaseMappingState(mapping = CaseMapping.RFC1459)

    @Before fun setUp() {
        channelsState = emptyChannelsState(caseMappingState)
        handler = Rpl471Handler(channelsState.joining, caseMappingState)
    }

    @Test fun test_handle_NonexistentChannel_DoesNothing() {
        handler.handle(Rpl471Message(source = "", target = "", channel = "#somewhere", contents = ""))

        assertEquals(emptyChannelsState(caseMappingState), channelsState)
    }

    @Test fun test_handle_ValidChannel_SetsStatusToFailed() {
        channelsState.joining += JoiningChannelState("#channel", status = JoiningChannelLifecycle.JOINING)

        handler.handle(Rpl471Message(source = "", target = "", channel = "#channel", contents = ""))

        val expectedChannelState = JoiningChannelState("#channel", status = JoiningChannelLifecycle.FAILED)

        assertEquals(joiningChannelsStateWith(listOf(expectedChannelState), caseMappingState), channelsState)
    }

}