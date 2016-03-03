package engineer.carrot.warren.warren.irc.messages.core;

import engineer.carrot.warren.warren.irc.messages.AbstractMessage;
import engineer.carrot.warren.warren.irc.messages.JavaIrcMessage;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;

public class PartChannelMessage extends AbstractMessage {
    public String channel;
    public String message;

    // Inbound

    @Override
    public boolean populate(JavaIrcMessage message) {
        // {"prefix":"test!~t@test","parameters":["#test","Part message"],"command":"PART"}
        // {"prefix":"AbcdefghIJK!~abcdef@111.111.11.11","parameters":["#test"],"command":"PART"}

        if (this.prefix == null || message.parameters.size() < 1) {
            return false;
        }

        this.channel = message.parameters.get(0);

        if (message.parameters.size() > 1) {
            this.message = message.parameters.get(1);
        } else {
            this.message = "";
        }

        return true;
    }

    // Shared

    @Override
    public String getCommand() {
        return MessageCodes.PART;
    }
}
