package engineer.carrot.warren.warren.handler.Rpl005

import engineer.carrot.warren.warren.state.ChannelModesState
import engineer.carrot.warren.warren.state.ChannelTypesState
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class Rpl005ChanTypesHandlerTests {

    lateinit var handler: IRpl005ChanTypesHandler
    lateinit var channelTypesState: ChannelTypesState
    val channelTypes = setOf('#', '&')
    val initialChannelTypesState = ChannelTypesState(channelTypes)

    @Before fun setUp() {
        handler = Rpl005ChanTypesHandler
        channelTypesState = ChannelTypesState(channelTypes)
    }

    @Test fun test_WellFormed_Defaults() {
        handler.handle("&#", channelTypesState)

        val channelTypes = setOf('&', '#')

        assertEquals(ChannelTypesState(channelTypes), channelTypesState)
    }

    @Test fun test_WellFormed_ArbitraryExamples() {
        handler.handle("!@£$%^&*()", channelTypesState)

        val channelTypes = setOf('!', '@', '£', '$', '%', '^', '&', '*', '(', ')')

        assertEquals(ChannelTypesState(channelTypes), channelTypesState)
    }

    @Test fun test_Malformed_EmptyString_DoesNotChangeState() {
        handler.handle("", channelTypesState)

        assertEquals(initialChannelTypesState, channelTypesState)
    }
}
