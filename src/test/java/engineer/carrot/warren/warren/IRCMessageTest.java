package engineer.carrot.warren.warren;

import com.google.common.collect.Lists;
import engineer.carrot.warren.warren.irc.messages.IrcMessage;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class IRCMessageTest {
    @Test
    public void testParseFromCommandOnly() {
        IrcMessage ircMessage = IrcMessage.parseFromLine("TEST");

        assertNotNull(ircMessage);
        assertEquals(ircMessage.command, "TEST");
        assertFalse(ircMessage.hasTags());
        assertFalse(ircMessage.hasPrefix());
        assertFalse(ircMessage.hasParameters());
    }

    @Test
    public void testParseFromPrefixCommand() {
        IrcMessage ircMessage = IrcMessage.parseFromLine(":user!host@server TEST");

        assertNotNull(ircMessage);
        assertEquals(ircMessage.command, "TEST");
        assertFalse(ircMessage.hasTags());
        assertTrue(ircMessage.hasPrefix());
        assertEquals(ircMessage.prefix, "user!host@server");
        assertFalse(ircMessage.hasParameters());
    }

    @Test
    public void testParseFromPrefixCommandParameters() {
        IrcMessage ircMessage = IrcMessage.parseFromLine(":user!host@server TEST some parameters :AND A LAST ONE");

        assertNotNull(ircMessage);
        assertEquals(ircMessage.command, "TEST");
        assertFalse(ircMessage.hasTags());
        assertTrue(ircMessage.hasPrefix());
        assertEquals(ircMessage.prefix, "user!host@server");
        assertEquals(ircMessage.parameters.size(), 3);
        assertEquals(ircMessage.parameters, Lists.newArrayList("some", "parameters", "AND A LAST ONE"));
    }

    @Test
    public void test_complexMode() {
        IrcMessage ircMessage = IrcMessage.parseFromLine(":subdomain.ircserver.net MODE +vvvv Nick1 Nick2 Nick3 Nick4");

        assertNotNull(ircMessage);
        assertEquals(ircMessage.command, "MODE");
        assertFalse(ircMessage.hasTags());
        assertTrue(ircMessage.hasPrefix());
        assertEquals(ircMessage.prefix, "subdomain.ircserver.net");
        assertEquals(ircMessage.parameters.size(), 5);
        assertEquals(ircMessage.parameters, Lists.newArrayList("+vvvv", "Nick1", "Nick2", "Nick3", "Nick4"));
    }

    @Test
    public void test_quit_netsplit() {
        IrcMessage ircMessage = IrcMessage.parseFromLine("QUIT :*.net *.split");

        assertNotNull(ircMessage);
        assertEquals(ircMessage.command, "QUIT");
        assertFalse(ircMessage.hasTags());
        assertFalse(ircMessage.hasPrefix());
        assertEquals(ircMessage.parameters.size(), 1);
        assertEquals(ircMessage.parameters, Lists.newArrayList("*.net *.split"));
    }

    @Test
    public void test_trailingWhitespaceIsRemoved() {
        IrcMessage ircMessage = IrcMessage.parseFromLine("QUIT one two three  ");

        assertNotNull(ircMessage);
        assertEquals(ircMessage.command, "QUIT");
        assertFalse(ircMessage.hasTags());
        assertFalse(ircMessage.hasPrefix());
        assertEquals(ircMessage.parameters.size(), 3);
        assertEquals(ircMessage.parameters, Lists.newArrayList("one", "two", "three"));
    }

    // TODO: Add tags unit testing

    @Test
    public void testBuildParametersSingle() throws Exception {
        String test1 = "Test1";

        List<String> parameters = Lists.newArrayList(test1);
        String stringParameters = IrcMessage.buildParametersString(parameters);

        // Expected output: ":Test1"
        assertEquals(":" + test1, stringParameters);
    }

    @Test
    public void testBuildParametersDouble() throws Exception {
        String test1 = "Test1";
        String test2 = "Test2";

        List<String> parameters = Lists.newArrayList(test1, test2);
        String stringParameters = IrcMessage.buildParametersString(parameters);

        // Expected output: "Test1 :Test2"
        assertEquals(test1 + " :" + test2, stringParameters);
    }

    @Test
    public void testBuildParametersTripleExtended() throws Exception {
        String test1 = "Test1";
        String test2 = "Test2";
        String test3 = "Test3 Test4 Test5";

        List<String> parameters = Lists.newArrayList(test1, test2, test3);
        String stringParameters = IrcMessage.buildParametersString(parameters);

        // Expected output: "Test1 Test2 :Test3 Test4 Test5"
        assertEquals(test1 + " " + test2 + " :" + test3, stringParameters);
    }
}