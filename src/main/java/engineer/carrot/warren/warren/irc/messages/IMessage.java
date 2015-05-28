package engineer.carrot.warren.warren.irc.messages;

import engineer.carrot.warren.warren.irc.messages.util.NoOpException;

import javax.annotation.Nonnull;

public interface IMessage {
    void populateFromIRCMessage(IRCMessage message);

    @Nonnull
    IMessage build(IRCMessage message);

    @Nonnull
    IRCMessage buildServerOutput();

    boolean isMessageWellFormed(@Nonnull IRCMessage message);

    @Nonnull
    String getCommandID();
}
