package engineer.carrot.warren.warren.irc.messages.core;

import engineer.carrot.warren.warren.irc.messages.AbstractMessage;
import engineer.carrot.warren.warren.irc.messages.IRCMessage;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;

public class ChangeNicknameMessage extends AbstractMessage {
    private String nickname;

    public ChangeNicknameMessage() {

    }

    public ChangeNicknameMessage(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public IRCMessage buildServerOutput() {
        return new IRCMessage.Builder().command(this.getCommandID()).parameters(this.nickname).build();
    }

    @Override
    public boolean isMessageWellFormed(IRCMessage message) {
        return (!message.isPrefixSet() && message.isParametersExactlyExpectedLength(1));
    }

    @Override
    public String getCommandID() {
        return MessageCodes.NICK;
    }
}
