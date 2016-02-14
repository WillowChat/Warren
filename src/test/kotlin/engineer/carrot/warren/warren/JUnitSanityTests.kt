package engineer.carrot.warren.warren

import org.junit.Assert
import org.junit.Test

class JUnitSanityTests {

    @Test fun test_sanityCheckJUnit_AssertsBooleanValuesCorrectly() {
        Assert.assertEquals(true, true)
        Assert.assertEquals(false, false)
        Assert.assertFalse(false)
        Assert.assertTrue(true)
    }
}