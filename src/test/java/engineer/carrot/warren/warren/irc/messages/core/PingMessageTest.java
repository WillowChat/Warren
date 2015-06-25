package engineer.carrot.warren.warren.irc.messages.core;

import engineer.carrot.warren.warren.irc.messages.IrcMessage;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;
import org.junit.Test;

import static org.junit.Assert.*;

public class PingMessageTest {
    // Inbound tests
    @Test
    public void testPingWithToken() {
        IrcMessage ircMessage = new IrcMessage.Builder()
                .command(MessageCodes.PING)
                .parameter("TOKEN1")
                .build();

        PingMessage message = new PingMessage();
        assertTrue(message.populate(ircMessage));
        assertEquals(message.pingToken, "TOKEN1");
    }

    // Outbound tests
    @Test
    public void testOutboundPingWithToken() {
        PingMessage message = new PingMessage("OUTBOUND1");

        IrcMessage ircMessage = message.build();

        assertTrue(ircMessage.hasParameters());
        assertEquals(ircMessage.parameters.get(0), "OUTBOUND1");
    }
}