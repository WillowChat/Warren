package engineer.carrot.warren.warren.irc.messages.core;

import engineer.carrot.warren.warren.irc.Hostmask;
import engineer.carrot.warren.warren.irc.messages.AbstractMessage;
import engineer.carrot.warren.warren.irc.messages.IRCMessage;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;

public class PartChannelMessage extends AbstractMessage {
    public Hostmask user;
    public String channel;
    public String message;

    @Override
    public void populateFromIRCMessage(IRCMessage message) {
        this.user = Hostmask.parseFromString(message.prefix);
        this.channel = message.parameters.get(0);

        if (message.parameters.size() > 1) {
            this.message = message.parameters.get(1);
        } else {
            this.message = "";
        }
    }

    @Override
    public boolean isMessageWellFormed(IRCMessage message) {
        // {"prefix":"test!~t@test","parameters":["#test","Part message"],"command":"PART"}
        // {"prefix":"AbcdefghIJK!~abcdef@111.111.11.11","parameters":["#test"],"command":"PART"}
        return (message.isPrefixSetAndNotEmpty() && message.isParametersAtLeastExpectedLength(1));
    }

    @Override
    public String getCommandID() {
        return MessageCodes.PART;
    }
}
