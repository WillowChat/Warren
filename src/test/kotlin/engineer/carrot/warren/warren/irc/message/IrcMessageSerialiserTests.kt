package engineer.carrot.warren.warren.irc.message

import org.junit.Assert.*
import org.junit.Test

class IrcMessageSerialiserTests {

    // Properly formed test cases

    @Test fun test_singleCommand() {
        val message = serialiseMessage(IrcMessage(command = "TEST"))

        assertEquals(message, "TEST")
    }

    @Test fun test_prefixAndCommand() {
        val message = serialiseMessage(IrcMessage(prefix = "user!host@server", command = "TEST"))

        assertEquals(message, ":user!host@server TEST")
    }

    @Test fun test_prefixCommandAndParameters() {
        val message = serialiseMessage(IrcMessage(prefix = "user!host@server", command = "TEST", parameters = listOf("some", "parameters", "And some trailing parameters! ")))

        assertEquals(message, ":user!host@server TEST some parameters :And some trailing parameters! ")
    }

    @Test fun test_commandAndTrailingParameters() {
        val message = serialiseMessage(IrcMessage(command = "TEST", parameters = listOf("Trailing parameters")))

        assertEquals(message, "TEST :Trailing parameters")
    }

    @Test fun test_trailingWhitespacePreserved() {
        val message = serialiseMessage(IrcMessage(command = "TEST", parameters = listOf("Trailing parameters with whitespace    ")))

        assertEquals(message, "TEST :Trailing parameters with whitespace    ")
    }

    @Test fun test_tags_specExample() {
        val message = serialiseMessage(IrcMessage(tags = mapOf("aaa" to "bbb", "ccc" to null, "example.com/ddd" to "eee"), prefix = "nick!ident@host.com", command = "PRIVMSG", parameters = listOf("me", "Hello")))

        assertEquals(message, "@aaa=bbb;ccc;example.com/ddd=eee :nick!ident@host.com PRIVMSG me :Hello")
    }

    @Test fun test_tags_singleTagNoValue() {
        val message = serialiseMessage(IrcMessage(tags = mapOf("test" to null), command = "TEST"))

        assertEquals(message, "@test TEST")
    }

    // Helper functions

    private fun serialiseMessage(message: IrcMessage): String? {
        return IrcMessageSerialiser.serialise(message)
    }
}