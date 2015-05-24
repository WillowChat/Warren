package engineer.carrot.warren.warren;

import engineer.carrot.warren.warren.irc.CharacterCodes;
import engineer.carrot.warren.warren.irc.Hostmask;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HostmaskTest {
    @Test
    public void testBuildHostmaskWithUserServerHost() {
        String hostmaskString = "USER!HOST@SERVER";
        Hostmask hostmask = Hostmask.parseFromString(hostmaskString);

        assertEquals(hostmask.user, "USER");
        assertEquals(hostmask.host, "HOST");
        assertEquals(hostmask.server, "SERVER");
    }

    @Test
    public void testBuildOutputStringUserOnly() throws Exception {
        Hostmask hostmask = new Hostmask.Builder().user("TESTUSER").build();

        assertEquals("TESTUSER", hostmask.buildOutputString());
    }

    @Test
    public void testBuildOutputStringUserHostServer() throws Exception {
        Hostmask hostmask = new Hostmask.Builder().user("TESTUSER").host("testhost").server("testserver").build();

        assertEquals("TESTUSER" + CharacterCodes.EXCLAM + "testhost" + CharacterCodes.AT + "testserver", hostmask.buildOutputString());
    }
}