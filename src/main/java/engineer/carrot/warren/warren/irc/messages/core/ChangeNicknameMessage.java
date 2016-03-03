package engineer.carrot.warren.warren.irc.messages.core;

import engineer.carrot.warren.warren.irc.messages.AbstractMessage;
import engineer.carrot.warren.warren.irc.messages.JavaIrcMessage;
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
    public boolean populate(JavaIrcMessage message) {
        if (!message.hasParameters()) {
            return false;
        }

        this.nickname = message.parameters.get(0);

        return true;
    }

    // Outbound

    @Override
    public JavaIrcMessage build() {
        JavaIrcMessage.Builder builder = new JavaIrcMessage.Builder()
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
