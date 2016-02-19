package engineer.carrot.warren.warren

import org.junit.Assert.*
import org.junit.Test

class JUnitSanityTests {

    @Test fun test_sanityCheckJUnit_AssertsBooleanValuesCorrectly() {
        assertEquals(true, true)
        assertEquals(false, false)
        assertFalse(false)
        assertTrue(true)
    }
}