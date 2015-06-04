package engineer.carrot.warren.warren.irc.messages.core;

import engineer.carrot.warren.warren.irc.messages.AbstractMessage;
import engineer.carrot.warren.warren.irc.messages.IRCMessage;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;

public class UserMessage extends AbstractMessage {
    private final String user;
    private final String mode;
    private final String realname;

    public UserMessage(String user, String mode, String realname) {
        this.user = user;
        this.mode = mode;
        this.realname = realname;
    }

    @Override
    public IRCMessage buildServerOutput() {
        return new IRCMessage.Builder().command(this.getCommandID()).parameters(this.user, this.mode, "*", this.realname).build();
    }

    @Override
    public boolean isMessageWellFormed(IRCMessage message) {
        return (!message.isPrefixSet() && message.isParametersExactlyExpectedLength(4));
    }

    @Override
    public String getCommandID() {
        return MessageCodes.USER;
    }
}
