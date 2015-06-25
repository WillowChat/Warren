package engineer.carrot.warren.warren.irc.messages.RPL;

import engineer.carrot.warren.warren.irc.messages.AbstractMessage;
import engineer.carrot.warren.warren.irc.messages.IrcMessage;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;

public class NoTopicMessage extends AbstractMessage {
    private String forUser;
    public String forChannel;
    private String contents;

    // Inbound

    @Override
    public boolean populate(IrcMessage message) {
        // Parsed message: {"prefix":"server","parameters":["bot nickname","#channel","no topic message,"command":"331"}

        if (!message.hasPrefix() || message.parameters.size() < 3) {
            return false;
        }

        this.forUser = message.parameters.get(0);
        this.forChannel = message.parameters.get(1);
        this.contents = message.parameters.get(2);

        return true;
    }

    // Shared

    @Override
    public String getCommand() {
        return MessageCodes.RPL.NOTOPIC;
    }
}
