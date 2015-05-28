package engineer.carrot.warren.warren.irc.messages.util;

import engineer.carrot.warren.warren.irc.messages.AbstractMessage;
import engineer.carrot.warren.warren.irc.messages.IMessage;
import engineer.carrot.warren.warren.irc.messages.IRCMessage;

public abstract class ServerTargetContentsMessage extends AbstractMessage {
    public String forServer;
    public String toTarget;
    public String contents;

    @Override
    public void populateFromIRCMessage(IRCMessage message) {
        this.forServer = message.prefix;
        this.toTarget = message.parameters.get(0);
        this.contents = message.parameters.get(1);
    }

    @Override
    public boolean isMessageWellFormed(IRCMessage message) {
        return (message.isPrefixSetAndNotEmpty() && message.isParametersExactlyExpectedLength(2));
    }
}
