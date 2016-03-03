package engineer.carrot.warren.warren.irc.messages.core;

import engineer.carrot.warren.warren.irc.messages.AbstractMessage;
import engineer.carrot.warren.warren.irc.messages.JavaIrcMessage;
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

    // Outbound

    @Override
    public JavaIrcMessage build() {
        return new JavaIrcMessage.Builder()
                .command(this.getCommand())
                .parameters(this.user, this.mode, "*", this.realname)
                .build();
    }

    // Shared

    @Override
    public String getCommand() {
        return MessageCodes.USER;
    }
}
