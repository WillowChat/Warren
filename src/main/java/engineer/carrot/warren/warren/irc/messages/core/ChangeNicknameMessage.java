package engineer.carrot.warren.warren.irc.messages.core;

import engineer.carrot.warren.warren.irc.Hostmask;
import engineer.carrot.warren.warren.irc.messages.AbstractMessage;
import engineer.carrot.warren.warren.irc.messages.IRCMessage;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;

import javax.annotation.Nullable;

public class ChangeNicknameMessage extends AbstractMessage {
    @Nullable
    public Hostmask fromUser;
    public String nickname;

    public ChangeNicknameMessage() {

    }

    public ChangeNicknameMessage(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public void populateFromIRCMessage(IRCMessage message) {
        this.fromUser = Hostmask.parseFromString(message.prefix);
        this.nickname = message.parameters.get(0);
    }

    @Override
    public IRCMessage buildServerOutput() {
        IRCMessage.Builder builder = new IRCMessage.Builder().command(this.getCommandID()).parameters(this.nickname);

        if (this.fromUser != null) {
            builder.prefix(this.fromUser.buildOutputString());
        }

        return builder.build();
    }

    @Override
    public boolean isMessageWellFormed(IRCMessage message) {
        return (message.isParametersExactlyExpectedLength(1));
    }

    @Override
    public String getCommandID() {
        return MessageCodes.NICK;
    }
}
