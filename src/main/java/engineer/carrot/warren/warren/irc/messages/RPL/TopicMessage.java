package engineer.carrot.warren.warren.irc.messages.RPL;

import engineer.carrot.warren.warren.irc.messages.AbstractMessage;
import engineer.carrot.warren.warren.irc.messages.IrcMessage;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;

public class TopicMessage extends AbstractMessage {
    private String forServer;
    private String forUser;
    public String forChannel;
    public String contents;

    @Override
    public void populateFromIRCMessage(IrcMessage message) {
        this.forServer = message.prefix;
        this.forUser = message.parameters.get(0);
        this.forChannel = message.parameters.get(1);
        this.contents = message.parameters.get(2);
    }

    @Override
    public boolean isMessageWellFormed(IrcMessage message) {
        // Parsed message: {"prefix":"server","parameters":["bot nickname","#channel","topic message,"command":"332"}
        return (message.isPrefixSetAndNotEmpty() && message.isParametersExactlyExpectedLength(3));
    }

    @Override
    public String getCommandID() {
        return MessageCodes.RPL.TOPIC;
    }
}
