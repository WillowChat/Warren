package engineer.carrot.warren.warren.irc.messages.RPL;

import engineer.carrot.warren.warren.irc.Hostmask;
import engineer.carrot.warren.warren.irc.messages.AbstractMessage;
import engineer.carrot.warren.warren.irc.messages.JavaIrcMessage;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;

public class TopicWhoTimeMessage extends AbstractMessage {
    private String forUser;
    private String forChannel;
    private Hostmask byUser;
    private long atTime;

    // Inbound

    @Override
    public boolean populate(JavaIrcMessage message) {
        if (!message.hasPrefix() || message.parameters.size() < 4) {
            return false;
        }

        this.forUser = message.parameters.get(0);
        this.forChannel = message.parameters.get(1);
        this.byUser = Hostmask.parseFromString(message.parameters.get(2));
        this.atTime = Long.valueOf(message.parameters.get(3));

        return true;
    }

    // Shared

    @Override
    public String getCommand() {
        return MessageCodes.RPL.TOPICWHOTIME;
    }
}
