package engineer.carrot.warren.warren.irc.messages.RPL;

import engineer.carrot.warren.warren.irc.messages.AbstractMessage;
import engineer.carrot.warren.warren.irc.messages.IRCMessage;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;

import javax.annotation.Nonnull;

public class TopicMessage extends AbstractMessage {
    public String forServer;
    public String forUser;
    public String forChannel;
    public String contents;

    @Override
    public void populateFromIRCMessage(IRCMessage message) {
        this.forServer = message.prefix;
        this.forUser = message.parameters.get(0);
        this.forChannel = message.parameters.get(1);
        this.contents = message.parameters.get(2);
    }

    @Override
    public boolean isMessageWellFormed(@Nonnull IRCMessage message) {
        // Parsed message: {"prefix":"server","parameters":["bot nickname","#channel","topic message,"command":"332"}
        return (message.isPrefixSetAndNotEmpty() && message.isParametersExactlyExpectedLength(3));
    }

    @Override
    public String getCommandID() {
        return MessageCodes.RPL.TOPIC;
    }
}
