package chat.willow.warren.handler.rpl.Rpl005

import chat.willow.warren.state.UserPrefixesState
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class Rpl005PrefixHandlerTests {

    lateinit var handler: IRpl005PrefixHandler
    lateinit var userPrefixesState: UserPrefixesState
    val initialPrefixesState = mapOf('@' to 'o', '+' to 'v')

    @Before fun setUp() {
        handler = Rpl005PrefixHandler
        userPrefixesState = UserPrefixesState(prefixesToModes = initialPrefixesState)
    }

    @Test fun test_OperatorVoiceAndHalfOpPrefixes() {
        handler.handle("(ovh)@+%", userPrefixesState)

        assertEquals(mapOf('@' to 'o', '+' to 'v', '%' to 'h'), userPrefixesState.prefixesToModes)
    }

    @Test fun test_Malformed_NoLeftBracket_DoesNotChangeState() {
        handler.handle("abc)!@£", userPrefixesState)

        assertEquals(initialPrefixesState, userPrefixesState.prefixesToModes)
    }

    @Test fun test_Malformed_NoRightBracket_DoesNotChangeState() {
        handler.handle("(abc!@£", userPrefixesState)

        assertEquals(initialPrefixesState, userPrefixesState.prefixesToModes)
    }

    @Test fun test_Malformed_NoModes_DoesNotChangeState() {
        handler.handle("()@+", userPrefixesState)

        assertEquals(initialPrefixesState, userPrefixesState.prefixesToModes)
    }

    @Test fun test_Malformed_NoPrefixes_DoesNotChangeState() {
        handler.handle("(ov)", userPrefixesState)

        assertEquals(initialPrefixesState, userPrefixesState.prefixesToModes)
    }

    @Test fun test_Malformed_MismatchedPrefixesAndModes_DoesNotChangeState() {
        handler.handle("(ov)@+!", userPrefixesState)

        assertEquals(initialPrefixesState, userPrefixesState.prefixesToModes)
    }

    @Test fun test_Malformed_EmptyString_DoesNotChangeState() {
        handler.handle("", userPrefixesState)

        assertEquals(initialPrefixesState, userPrefixesState.prefixesToModes)
    }
}