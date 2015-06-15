package engineer.carrot.warren.warren.irc.messages;

import engineer.carrot.warren.warren.irc.handlers.RPL.isupport.IISupportManager;

public interface IMessage {
    void setISupportManager(IISupportManager manager);

    void populateFromIRCMessage(IrcMessage message);

    IMessage build(IrcMessage message);

    IrcMessage buildServerOutput();

    boolean isMessageWellFormed(IrcMessage message);

    String getCommandID();
}
