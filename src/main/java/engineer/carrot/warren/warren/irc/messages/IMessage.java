package engineer.carrot.warren.warren.irc.messages;

public interface IMessage {
    void populateFromIRCMessage(IRCMessage message);

    IMessage build(IRCMessage message);

    IRCMessage buildServerOutput();

    boolean isMessageWellFormed(IRCMessage message);

    String getCommandID();
}
