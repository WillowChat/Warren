package engineer.carrot.warren.warren.irc.messages;

public interface IMessage {
    void populateFromIRCMessage(IrcMessage message);

    IMessage build(IrcMessage message);

    IrcMessage buildServerOutput();

    boolean isMessageWellFormed(IrcMessage message);

    String getCommandID();
}
