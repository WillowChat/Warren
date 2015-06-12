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
        assertTrue(ircMessage.isCommandSet());
        assertEquals(ircMessage.command, "TEST");
        assertFalse(ircMessage.isTagsSet());
        assertFalse(ircMessage.isPrefixSet());
        assertFalse(ircMessage.isParametersSet());
    }

    @Test
    public void testParseFromPrefixCommand() {
        IrcMessage ircMessage = IrcMessage.parseFromLine(":user!host@server TEST");

        assertNotNull(ircMessage);
        assertTrue(ircMessage.isCommandSet());
        assertEquals(ircMessage.command, "TEST");
        assertFalse(ircMessage.isTagsSet());
        assertTrue(ircMessage.isPrefixSetAndNotEmpty());
        assertEquals(ircMessage.prefix, "user!host@server");
        assertFalse(ircMessage.isParametersSet());
    }

    @Test
    public void testParseFromPrefixCommandParameters() {
        IrcMessage ircMessage = IrcMessage.parseFromLine(":user!host@server TEST some parameters :AND A LAST ONE");

        assertNotNull(ircMessage);
        assertTrue(ircMessage.isCommandSet());
        assertEquals(ircMessage.command, "TEST");
        assertFalse(ircMessage.isTagsSet());
        assertTrue(ircMessage.isPrefixSet());
        assertEquals(ircMessage.prefix, "user!host@server");
        assertTrue(ircMessage.isParametersExactlyExpectedLength(3));
        assertEquals(ircMessage.parameters, Lists.newArrayList("some", "parameters", "AND A LAST ONE"));
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