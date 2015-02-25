package engineer.carrot.warren.irc.messages.core;

import engineer.carrot.warren.irc.Hostmask;
import engineer.carrot.warren.irc.messages.IRCMessage;
import engineer.carrot.warren.irc.messages.MessageCodes;
import engineer.carrot.warren.irc.messages.IMessage;

import javax.annotation.Nonnull;

public class PartChannelMessage implements IMessage {
    public Hostmask user;
    public String channel;
    public String message;

    @Override
    public void populateFromIRCMessage(IRCMessage message) {
        this.user = Hostmask.parseFromString(message.prefix);
        this.channel = message.parameters.get(0);
        this.message = message.parameters.get(1);
    }

    @Override
    public boolean isMessageWellFormed(@Nonnull IRCMessage message) {
        // {"prefix":"test!~t@test","parameters":["#test","Part message"],"command":"PART"}
        return (message.isPrefixSetAndNotEmpty() && message.isParametersExactlyExpectedLength(2));
    }

    @Override
    public String getCommandID() {
        return MessageCodes.PART;
    }
}
