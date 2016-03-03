package engineer.carrot.warren.warren.irc.messages.core;

import engineer.carrot.warren.warren.irc.messages.AbstractMessage;
import engineer.carrot.warren.warren.irc.messages.JavaIrcMessage;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;

public class NoticeMessage extends AbstractMessage {
    private String toTarget;
    private String contents;

    public NoticeMessage() {

    }

    // Inbound

    @Override
    public boolean populate(JavaIrcMessage message) {
        // {"prefix":"server","parameters":["*","contents"],"command":"NOTICE"}
        // {"parameters":["*","contents"],"command":"NOTICE"}

        if (message.parameters.size() < 2) {
            return false;
        }

        this.toTarget = message.parameters.get(0);
        this.contents = message.parameters.get(1);

        return true;
    }

    // Outbound

    @Override
    public JavaIrcMessage build() {
        JavaIrcMessage.Builder builder = new JavaIrcMessage.Builder()
                .command(this.getCommand())
                .parameters(this.toTarget, this.contents);

        if (this.prefix != null) {
            builder.prefix(this.prefix);
        }

        return builder.build();
    }

    // Shared

    @Override
    public String getCommand() {
        return MessageCodes.NOTICE;
    }
}
