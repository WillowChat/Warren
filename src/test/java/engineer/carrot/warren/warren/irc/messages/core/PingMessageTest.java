package engineer.carrot.warren.warren.irc.messages.core;

import engineer.carrot.warren.warren.irc.messages.JavaIrcMessage;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;
import org.junit.Test;

import static org.junit.Assert.*;

public class PingMessageTest {
    // Inbound tests
    @Test
    public void testPingWithToken() {
        JavaIrcMessage javaIrcMessage = new JavaIrcMessage.Builder()
                .command(MessageCodes.PING)
                .parameter("TOKEN1")
                .build();

        PingMessage message = new PingMessage();
        assertTrue(message.populate(javaIrcMessage));
        assertEquals(message.pingToken, "TOKEN1");
    }

    // Outbound tests
    @Test
    public void testOutboundPingWithToken() {
        PingMessage message = new PingMessage("OUTBOUND1");

        JavaIrcMessage javaIrcMessage = message.build();

        assertTrue(javaIrcMessage.hasParameters());
        assertEquals(javaIrcMessage.parameters.get(0), "OUTBOUND1");
    }
}