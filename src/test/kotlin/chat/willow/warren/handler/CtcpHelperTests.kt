package chat.willow.warren.handler

import engineer.carrot.warren.kale.irc.CharacterCodes
import org.junit.Assert.*
import org.junit.Test

class CtcpEnumTests {

    @Test fun test_from_None() {
        val ctcp = CtcpEnum.from("something")

        assertEquals(CtcpEnum.NONE, ctcp)
    }

    @Test fun test_from_Action() {
        val ctcp = CtcpEnum.from("${CharacterCodes.CTCP}ACTION something${CharacterCodes.CTCP}")

        assertEquals(CtcpEnum.ACTION, ctcp)
    }

    @Test fun test_from_Unknown() {
        val ctcp = CtcpEnum.from("${CharacterCodes.CTCP}UNKNOWN ${CharacterCodes.CTCP}")

        assertEquals(CtcpEnum.UNKNOWN, ctcp)
    }

}

class CtcpHelperTests {

    @Test fun test_isMessageCTCP_EmptyString() {
        val isCtcp = CtcpHelper.isMessageCTCP("")

        assertFalse(isCtcp)
    }

    @Test fun test_isMessageCTCP_SingleCTCPCharacter() {
        val isCtcp = CtcpHelper.isMessageCTCP("${CharacterCodes.CTCP}")

        assertTrue(isCtcp)
    }

    @Test fun test_isMessageCTCP_WellFormed() {
        val isCtcp = CtcpHelper.isMessageCTCP("${CharacterCodes.CTCP}SOMETHING ${CharacterCodes.CTCP}")

        assertTrue(isCtcp)
    }

    @Test fun test_trimCTCP_EmptyString() {
        val trimmed = CtcpHelper.trimCTCP("")

        assertEquals("", trimmed)
    }

    @Test fun test_trimCTCP_SingleCTCPCharacter() {
        val trimmed = CtcpHelper.trimCTCP("${CharacterCodes.CTCP}")

        assertEquals("", trimmed)
    }

    @Test fun test_trimCTCP_TwoCTCPCharacters() {
        val trimmed = CtcpHelper.trimCTCP("${CharacterCodes.CTCP}${CharacterCodes.CTCP}")

        assertEquals("", trimmed)
    }

    @Test fun test_trimCTCP_WellFormedACTION() {
        val trimmed = CtcpHelper.trimCTCP("${CharacterCodes.CTCP}ACTION something${CharacterCodes.CTCP}")

        assertEquals("something", trimmed)
    }

    @Test fun test_trimCTCP_WellFormedUNKNOWN() {
        val trimmed = CtcpHelper.trimCTCP("${CharacterCodes.CTCP}UNKNOWN something else${CharacterCodes.CTCP}")

        assertEquals("something else", trimmed)
    }

}