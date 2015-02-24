package engineer.carrot.warren.irc.messages.core;

import com.google.common.collect.Lists;
import engineer.carrot.warren.IRCMessage;
import engineer.carrot.warren.irc.messages.MessageCodes;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class PingMessageTest implements IMessageTest {
    @Test
    public void isMessageWellFormedTest() {
        PingMessage message = new PingMessage();
        assertTrue(message.isMessageWellFormed(new IRCMessage.Builder().command(MessageCodes.PING).parameters("TOKEN1").build()));
        assertFalse(message.isMessageWellFormed(new IRCMessage.Builder().command(MessageCodes.PING).parameters("TOKEN1", "BAD PARAM").build()));
    }

    public static class PingMessageSerialiserTest extends AbstractMessageSerialiserTest<PingMessage> {
        @Override
        public PingMessage createMessage() throws IllegalAccessException, InstantiationException {
            return PingMessage.class.newInstance();
        }

        @Override
        public List<IRCMessage> constructTestCases() {
            IRCMessage testOne = new IRCMessage.Builder().command(MessageCodes.PING).parameters("TOKEN1").build();
            IRCMessage testTwo = new IRCMessage.Builder().command(MessageCodes.PING).parameters("TOKEN2").build();

            return Lists.newArrayList(testOne, testTwo);
        }

        @Override
        public void testMessageSerialisation(IRCMessage ircMessage, PingMessage message) {
            assertNotNull(message.pingToken);
            assertEquals(message.pingToken, ircMessage.parameters.get(0));
        }
    }

    public static class PingMessageDeserialiserTest extends AbstractMessageDeserialiserTest<PingMessage> {
        @Override
        public List<PingMessage> constructTestCases() {
            PingMessage testOne = new PingMessage("TOKEN1");
            PingMessage testTwo = new PingMessage("TOKEN2");

            return Lists.newArrayList(testOne, testTwo);
        }

        @Override
        public void testMessageSerialisation(PingMessage message, IRCMessage ircMessage) {
            assertFalse(ircMessage.isPrefixSetAndNotEmpty());
            assertFalse(ircMessage.isTagsSet());
            assertTrue(ircMessage.isParametersExactlyExpectedLength(1));
            assertEquals(ircMessage.command, MessageCodes.PING);
            assertEquals(message.pingToken, ircMessage.parameters.get(0));
        }
    }
}