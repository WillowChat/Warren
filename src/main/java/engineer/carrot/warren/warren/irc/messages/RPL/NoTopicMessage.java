package engineer.carrot.warren.warren.irc.messages.RPL;

import engineer.carrot.warren.warren.irc.messages.AbstractMessage;
import engineer.carrot.warren.warren.irc.messages.IrcMessage;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;

public class NoTopicMessage extends AbstractMessage {
    private String forServer;
    private String forUser;
    public String forChannel;
    private String contents;

    @Override
    public void populateFromIRCMessage(IrcMessage message) {
        this.forServer = message.prefix;
        this.forUser = message.parameters.get(0);
        this.forChannel = message.parameters.get(1);
        this.contents = message.parameters.get(2);
    }

    @Override
    public boolean isMessageWellFormed(IrcMessage message) {
        // Parsed message: {"prefix":"server","parameters":["bot nickname","#channel","no topic message,"command":"331"}
        return (message.isPrefixSetAndNotEmpty() && message.isParametersExactlyExpectedLength(3));
    }

    @Override
    public String getCommandID() {
        return MessageCodes.RPL.NOTOPIC;
    }
}
