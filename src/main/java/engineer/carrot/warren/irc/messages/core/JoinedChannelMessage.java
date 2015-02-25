package engineer.carrot.warren.irc.messages.core;

import engineer.carrot.warren.irc.Hostmask;
import engineer.carrot.warren.irc.messages.IRCMessage;
import engineer.carrot.warren.irc.messages.IMessage;
import engineer.carrot.warren.irc.messages.MessageCodes;

import javax.annotation.Nonnull;

public class JoinedChannelMessage implements IMessage {
    public Hostmask forUser;
    public String toTarget;

    @Override
    public void populateFromIRCMessage(IRCMessage message) {
        // {"prefix":"test!~t@test","parameters":["#test"],"command":"JOIN"}

        this.forUser = Hostmask.parseFromString(message.prefix);
        this.toTarget = message.parameters.get(0);
    }

    @Override
    public boolean isMessageWellFormed(@Nonnull IRCMessage message) {
        // {"prefix":"test!~t@test","parameters":["#test"],"command":"JOIN"}
        return (message.isPrefixSetAndNotEmpty() && message.isParametersExactlyExpectedLength(1));
    }

    @Override
    public String getCommandID() {
        return MessageCodes.JOIN;
    }
}
