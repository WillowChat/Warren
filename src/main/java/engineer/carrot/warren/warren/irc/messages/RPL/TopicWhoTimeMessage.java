package engineer.carrot.warren.warren.irc.messages.RPL;

import engineer.carrot.warren.warren.irc.Hostmask;
import engineer.carrot.warren.warren.irc.messages.AbstractMessage;
import engineer.carrot.warren.warren.irc.messages.IrcMessage;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;

public class TopicWhoTimeMessage extends AbstractMessage {
    private String forServer;
    private String forUser;
    private String forChannel;
    private Hostmask byUser;
    private long atTime;

    @Override
    public void populateFromIRCMessage(IrcMessage message) {
        this.forServer = message.prefix;
        this.forUser = message.parameters.get(0);
        this.forChannel = message.parameters.get(1);
        this.byUser = Hostmask.parseFromString(message.parameters.get(2));
        this.atTime = Long.valueOf(message.parameters.get(3));
    }

    @Override
    public boolean isMessageWellFormed(IrcMessage message) {
        return (message.isPrefixSetAndNotEmpty() && message.isParametersExactlyExpectedLength(4));
    }

    @Override
    public String getCommandID() {
        return MessageCodes.RPL.TOPICWHOTIME;
    }
}
