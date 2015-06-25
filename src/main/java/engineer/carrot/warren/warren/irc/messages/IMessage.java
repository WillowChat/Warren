package engineer.carrot.warren.warren.irc.messages;

import engineer.carrot.warren.warren.irc.handlers.RPL.isupport.IISupportManager;

public interface IMessage {
    void setISupportManager(IISupportManager manager);

    boolean populate(IrcMessage message);

    IMessage build(IrcMessage message);

    IrcMessage build();

    String getCommand();
}
