package engineer.carrot.warren.warren.handler.Rpl005

import engineer.carrot.warren.warren.state.ChannelModesState
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class Rpl005ChanModesHandlerTests {

    lateinit var handler: IRpl005ChanModesHandler
    lateinit var channelModesState: ChannelModesState
    val initialTypeA = setOf('e', 'I', 'b')
    val initialTypeB = setOf('k')
    val initialTypeC = setOf('l')
    val initialTypeD = setOf('i', 'm', 'n', 'p', 's', 't', 'S', 'r')
    val initialChannelModesState = ChannelModesState(initialTypeA, initialTypeB, initialTypeC, initialTypeD)

    @Before fun setUp() {
        handler = Rpl005ChanModesHandler
        channelModesState = ChannelModesState(initialTypeA, initialTypeB, initialTypeC, initialTypeD)
    }

    @Test fun test_WellFormed_Defaults() {
        handler.handle("eIb,k,l,imnpstSr", channelModesState)

        val expectedTypeA = setOf('e', 'I', 'b')
        val expectedTypeB = setOf('k')
        val expectedTypeC = setOf('l')
        val expectedTypeD = setOf('i', 'm', 'n', 'p', 's', 't', 'S', 'r')

        assertEquals(ChannelModesState(expectedTypeA, expectedTypeB, expectedTypeC, expectedTypeD), channelModesState)
    }

    @Test fun test_WellFormed_EmptyModes() {
        handler.handle("a,bc,,e", channelModesState)

        val expectedTypeA = setOf('a')
        val expectedTypeB = setOf('b', 'c')
        val expectedTypeC = setOf<Char>()
        val expectedTypeD = setOf('e')

        assertEquals(ChannelModesState(expectedTypeA, expectedTypeB, expectedTypeC, expectedTypeD), channelModesState)
    }

    @Test fun test_Malformed_ThreeTypes_DoesNotChangeState() {
        handler.handle("abc,d,e", channelModesState)

        assertEquals(initialChannelModesState, channelModesState)
    }

    @Test fun test_Malformed_FiveTypes_UsesFirstFourTypesOnly() {
        handler.handle("a,b,c,de,fg", channelModesState)

        val expectedTypeA = setOf('a')
        val expectedTypeB = setOf('b')
        val expectedTypeC = setOf('c')
        val expectedTypeD = setOf('d', 'e')

        assertEquals(ChannelModesState(expectedTypeA, expectedTypeB, expectedTypeC, expectedTypeD), channelModesState)
    }

    @Test fun test_Malformed_EmptyString_DoesNotChangeState() {
        handler.handle("", channelModesState)

        assertEquals(initialChannelModesState, channelModesState)
    }
}
