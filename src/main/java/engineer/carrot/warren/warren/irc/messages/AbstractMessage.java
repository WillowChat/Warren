package engineer.carrot.warren.warren.irc.messages;

import engineer.carrot.warren.warren.irc.messages.util.NoOpException;

import javax.annotation.Nonnull;

public abstract class AbstractMessage implements IMessage {
    @Override
    public void populateFromIRCMessage(IRCMessage message) {
        throw new NoOpException();
    }

    @Override
    @Nonnull
    public IMessage build(IRCMessage message) {
        this.populateFromIRCMessage(message);

        return this;
    }

    @Override
    @Nonnull
    public IRCMessage buildServerOutput() {
        throw new NoOpException();
    }
}
