package engineer.carrot.warren.warren;

import com.google.common.collect.Lists;
import engineer.carrot.warren.warren.irc.messages.JavaIrcMessage;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class IRCMessageTest {
    @Test
    public void testParseFromCommandOnly() {
        JavaIrcMessage javaIrcMessage = JavaIrcMessage.parseFromLine("TEST");

        assertNotNull(javaIrcMessage);
        assertEquals(javaIrcMessage.command, "TEST");
        assertFalse(javaIrcMessage.hasTags());
        assertFalse(javaIrcMessage.hasPrefix());
        assertFalse(javaIrcMessage.hasParameters());
    }

    @Test
    public void testParseFromPrefixCommand() {
        JavaIrcMessage javaIrcMessage = JavaIrcMessage.parseFromLine(":user!host@server TEST");

        assertNotNull(javaIrcMessage);
        assertEquals(javaIrcMessage.command, "TEST");
        assertFalse(javaIrcMessage.hasTags());
        assertTrue(javaIrcMessage.hasPrefix());
        assertEquals(javaIrcMessage.prefix, "user!host@server");
        assertFalse(javaIrcMessage.hasParameters());
    }

    @Test
    public void testParseFromPrefixCommandParameters() {
        JavaIrcMessage javaIrcMessage = JavaIrcMessage.parseFromLine(":user!host@server TEST some parameters :AND A LAST ONE");

        assertNotNull(javaIrcMessage);
        assertEquals(javaIrcMessage.command, "TEST");
        assertFalse(javaIrcMessage.hasTags());
        assertTrue(javaIrcMessage.hasPrefix());
        assertEquals(javaIrcMessage.prefix, "user!host@server");
        assertEquals(javaIrcMessage.parameters.size(), 3);
        assertEquals(javaIrcMessage.parameters, Lists.newArrayList("some", "parameters", "AND A LAST ONE"));
    }

    @Test
    public void test_complexMode() {
        JavaIrcMessage javaIrcMessage = JavaIrcMessage.parseFromLine(":subdomain.ircserver.net MODE +vvvv Nick1 Nick2 Nick3 Nick4");

        assertNotNull(javaIrcMessage);
        assertEquals(javaIrcMessage.command, "MODE");
        assertFalse(javaIrcMessage.hasTags());
        assertTrue(javaIrcMessage.hasPrefix());
        assertEquals(javaIrcMessage.prefix, "subdomain.ircserver.net");
        assertEquals(javaIrcMessage.parameters.size(), 5);
        assertEquals(javaIrcMessage.parameters, Lists.newArrayList("+vvvv", "Nick1", "Nick2", "Nick3", "Nick4"));
    }

    @Test
    public void test_quit_netsplit() {
        JavaIrcMessage javaIrcMessage = JavaIrcMessage.parseFromLine("QUIT :*.net *.split");

        assertNotNull(javaIrcMessage);
        assertEquals(javaIrcMessage.command, "QUIT");
        assertFalse(javaIrcMessage.hasTags());
        assertFalse(javaIrcMessage.hasPrefix());
        assertEquals(javaIrcMessage.parameters.size(), 1);
        assertEquals(javaIrcMessage.parameters, Lists.newArrayList("*.net *.split"));
    }

    @Test
    public void test_trailingWhitespaceIsRemoved() {
        JavaIrcMessage javaIrcMessage = JavaIrcMessage.parseFromLine("QUIT one two three  ");

        assertNotNull(javaIrcMessage);
        assertEquals(javaIrcMessage.command, "QUIT");
        assertFalse(javaIrcMessage.hasTags());
        assertFalse(javaIrcMessage.hasPrefix());
        assertEquals(javaIrcMessage.parameters.size(), 3);
        assertEquals(javaIrcMessage.parameters, Lists.newArrayList("one", "two", "three"));
    }


    @Test
    public void test_whitespaceAfterCommandIsRemoved() {
        JavaIrcMessage javaIrcMessage = JavaIrcMessage.parseFromLine("QUIT   one two three  ");

        assertNotNull(javaIrcMessage);
        assertEquals(javaIrcMessage.command, "QUIT");
        assertFalse(javaIrcMessage.hasTags());
        assertFalse(javaIrcMessage.hasPrefix());
        assertEquals(javaIrcMessage.parameters.size(), 3);
        assertEquals(javaIrcMessage.parameters, Lists.newArrayList("one", "two", "three"));
    }

    @Test
    public void test_whitespaceBetweenParametersIsRemoved() {
        JavaIrcMessage javaIrcMessage = JavaIrcMessage.parseFromLine("QUIT   one two      three  ");

        assertNotNull(javaIrcMessage);
        assertEquals(javaIrcMessage.command, "QUIT");
        assertFalse(javaIrcMessage.hasTags());
        assertFalse(javaIrcMessage.hasPrefix());
        assertEquals(javaIrcMessage.parameters.size(), 3);
        assertEquals(javaIrcMessage.parameters, Lists.newArrayList("one", "two", "three"));
    }

    @Test
    public void test_trailingWhitespaceWithColonIsPreserved() {
        JavaIrcMessage javaIrcMessage = JavaIrcMessage.parseFromLine("QUIT   one two      :three  ");

        assertNotNull(javaIrcMessage);
        assertEquals(javaIrcMessage.command, "QUIT");
        assertFalse(javaIrcMessage.hasTags());
        assertFalse(javaIrcMessage.hasPrefix());
        assertEquals(javaIrcMessage.parameters.size(), 3);
        assertEquals(javaIrcMessage.parameters, Lists.newArrayList("one", "two", "three  "));
    }

    // TODO: Add tags unit testing

    @Test
    public void testBuildParametersSingle() throws Exception {
        String test1 = "Test1";

        List<String> parameters = Lists.newArrayList(test1);
        String stringParameters = JavaIrcMessage.buildParametersString(parameters);

        // Expected output: ":Test1"
        assertEquals(":" + test1, stringParameters);
    }

    @Test
    public void testBuildParametersDouble() throws Exception {
        String test1 = "Test1";
        String test2 = "Test2";

        List<String> parameters = Lists.newArrayList(test1, test2);
        String stringParameters = JavaIrcMessage.buildParametersString(parameters);

        // Expected output: "Test1 :Test2"
        assertEquals(test1 + " :" + test2, stringParameters);
    }

    @Test
    public void testBuildParametersTripleExtended() throws Exception {
        String test1 = "Test1";
        String test2 = "Test2";
        String test3 = "Test3 Test4 Test5";

        List<String> parameters = Lists.newArrayList(test1, test2, test3);
        String stringParameters = JavaIrcMessage.buildParametersString(parameters);

        // Expected output: "Test1 Test2 :Test3 Test4 Test5"
        assertEquals(test1 + " " + test2 + " :" + test3, stringParameters);
    }
}