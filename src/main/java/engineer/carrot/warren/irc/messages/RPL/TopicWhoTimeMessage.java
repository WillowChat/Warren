package engineer.carrot.warren.irc.messages.RPL;

import engineer.carrot.warren.irc.Hostmask;
import engineer.carrot.warren.irc.messages.IMessage;
import engineer.carrot.warren.irc.messages.IRCMessage;
import engineer.carrot.warren.irc.messages.MessageCodes;

import javax.annotation.Nonnull;

public class TopicWhoTimeMessage implements IMessage {
    public String forServer;
    public String forUser;
    public String forChannel;
    public Hostmask byUser;
    public long atTime;

    @Override
    public void populateFromIRCMessage(IRCMessage message) {
        this.forServer = message.prefix;
        this.forUser = message.parameters.get(0);
        this.forChannel = message.parameters.get(1);
        this.byUser = Hostmask.parseFromString(message.parameters.get(2));
        this.atTime = Long.valueOf(message.parameters.get(3));
    }

    @Override
    public boolean isMessageWellFormed(IRCMessage message) {
        return (message.isPrefixSetAndNotEmpty() && message.isParametersExactlyExpectedLength(4));
    }

    @Nonnull
    @Override
    public String getCommandID() {
        return MessageCodes.RPL.TOPICWHOTIME;
    }
}
