package engineer.carrot.warren.warren.irc.messages;

import engineer.carrot.warren.warren.irc.messages.util.NoOpException;

public abstract class AbstractMessage implements IMessage {
    @Override
    public void populateFromIRCMessage(IRCMessage message) {
        throw new NoOpException();
    }

    @Override
    public IMessage build(IRCMessage message) {
        this.populateFromIRCMessage(message);

        return this;
    }

    @Override
    public IRCMessage buildServerOutput() {
        throw new NoOpException();
    }
}
