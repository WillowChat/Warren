package engineer.carrot.warren.irc.messages.core;

import engineer.carrot.warren.IRCMessage;
import engineer.carrot.warren.irc.messages.IMessage;
import engineer.carrot.warren.irc.messages.util.NoOpException;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.List;

public abstract class AbstractMessageDeserialiserTest<M extends IMessage> {
    private List<M> testCases;

    @Before
    public void setup() throws Exception {
        this.testCases = this.constructTestCases();
    }

    @Test
    public void testSerialisation() throws Exception {
        for (int i = 0; i < this.testCases.size(); i++) {
            IRCMessage ircMessage = this.createAndPopulateMessage(this.testCases.get(i));
            this.testMessageSerialisation(this.testCases.get(i), ircMessage);
        }
    }

    @Nullable
    private IRCMessage createAndPopulateMessage(M message) throws NoOpException {
        return message.buildServerOutput();
    }

    public abstract List<M> constructTestCases();

    public abstract void testMessageSerialisation(M message, IRCMessage ircMessage);
}
