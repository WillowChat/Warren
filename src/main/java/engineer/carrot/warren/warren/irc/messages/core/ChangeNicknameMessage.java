package engineer.carrot.warren.warren.irc.messages.core;

import engineer.carrot.warren.warren.irc.messages.AbstractMessage;
import engineer.carrot.warren.warren.irc.messages.IrcMessage;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;

public class ChangeNicknameMessage extends AbstractMessage {
    public String nickname;

    public ChangeNicknameMessage() {

    }

    public ChangeNicknameMessage(String nickname) {
        this.nickname = nickname;
    }

    // Inbound

    @Override
    public boolean populate(IrcMessage message) {
        if (!message.hasParameters()) {
            return false;
        }

        this.nickname = message.parameters.get(0);

        return true;
    }

    // Outbound

    @Override
    public IrcMessage build() {
        IrcMessage.Builder builder = new IrcMessage.Builder()
                .command(this.getCommand())
                .parameters(this.nickname);

        if (this.prefix != null) {
            builder.prefix(this.prefix.buildOutputString());
        }

        return builder.build();
    }

    // Shared

    @Override
    public String getCommand() {
        return MessageCodes.NICK;
    }
}
