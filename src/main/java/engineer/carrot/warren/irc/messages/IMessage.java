package engineer.carrot.warren.irc.messages;

import engineer.carrot.warren.irc.messages.util.NoOpException;

import javax.annotation.Nonnull;

public interface IMessage {
    public default void populateFromIRCMessage(IRCMessage message) {
        throw new NoOpException();
    }

    @Nonnull
    public default IMessage build(IRCMessage message) {
        this.populateFromIRCMessage(message);

        return this;
    }

    @Nonnull
    public default IRCMessage buildServerOutput() {
        throw new NoOpException();
    }

    public boolean isMessageWellFormed(@Nonnull IRCMessage message);

    @Nonnull
    public String getCommandID();
}
