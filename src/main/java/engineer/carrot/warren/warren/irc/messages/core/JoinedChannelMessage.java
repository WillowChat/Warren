package engineer.carrot.warren.warren.irc.messages.core;

import engineer.carrot.warren.warren.irc.messages.AbstractMessage;
import engineer.carrot.warren.warren.irc.messages.JavaIrcMessage;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;

public class JoinedChannelMessage extends AbstractMessage {
    public String channel;

    // Inbound

    @Override
    public boolean populate(JavaIrcMessage message) {
        // {"prefix":"test!~t@test","parameters":["#test"],"command":"JOIN"}
        if (this.prefix == null || !message.hasParameters()) {
            return false;
        }

        this.channel = message.parameters.get(0);

        return true;
    }

    // Shared

    @Override
    public String getCommand() {
        return MessageCodes.JOIN;
    }
}
