package engineer.carrot.warren.warren.irc.messages.core;

import engineer.carrot.warren.warren.irc.Hostmask;
import engineer.carrot.warren.warren.irc.messages.AbstractMessage;
import engineer.carrot.warren.warren.irc.messages.IrcMessage;
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
    public void populateFromIRCMessage(IrcMessage message) {
        this.fromUser = Hostmask.parseFromString(message.prefix);
        this.nickname = message.parameters.get(0);
    }

    @Override
    public IrcMessage buildServerOutput() {
        IrcMessage.Builder builder = new IrcMessage.Builder().command(this.getCommandID()).parameters(this.nickname);

        if (this.fromUser != null) {
            builder.prefix(this.fromUser.buildOutputString());
        }

        return builder.build();
    }

    @Override
    public boolean isMessageWellFormed(IrcMessage message) {
        return (message.isParametersExactlyExpectedLength(1));
    }

    @Override
    public String getCommandID() {
        return MessageCodes.NICK;
    }
}
