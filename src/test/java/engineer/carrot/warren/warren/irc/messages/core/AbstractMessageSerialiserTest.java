package engineer.carrot.warren.warren.irc.messages.core;

import engineer.carrot.warren.warren.irc.messages.IMessage;
import engineer.carrot.warren.warren.irc.messages.IrcMessage;
import engineer.carrot.warren.warren.irc.messages.util.NoOpException;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.List;

public abstract class AbstractMessageSerialiserTest<M extends IMessage> {
    private List<IrcMessage> testCases;

    public abstract M createMessage() throws IllegalAccessException, InstantiationException;

    @Before
    public void setup() throws Exception {
        this.testCases = this.constructTestCases();
    }

    @Test
    public void testSerialisation() throws Exception {
        for (int i = 0; i < this.testCases.size(); i++) {
            M message = this.createAndPopulateMessage(this.testCases.get(i));
            this.testMessageSerialisation(this.testCases.get(i), message);
        }
    }

    @Nullable
    private M createAndPopulateMessage(IrcMessage ircMessage) throws NoOpException, IllegalAccessException, InstantiationException {
        M message = this.createMessage();
        message.populateFromIRCMessage(ircMessage);

        return message;
    }

    public abstract List<IrcMessage> constructTestCases();

    public abstract void testMessageSerialisation(IrcMessage ircMessage, M message);
}
