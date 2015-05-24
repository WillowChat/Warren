package engineer.carrot.warren.warren.irc.messages.core;

import engineer.carrot.warren.warren.irc.messages.IMessage;
import engineer.carrot.warren.warren.irc.messages.IRCMessage;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;

import javax.annotation.Nonnull;

public class PongMessage implements IMessage {
    private String pongToken;

    public PongMessage() {

    }

    public PongMessage(String pongToken) {
        this.pongToken = pongToken;
    }

    @Override
    public IRCMessage buildServerOutput() {
        return new IRCMessage.Builder().command(this.getCommandID()).parameters(this.pongToken).build();
    }

    @Override
    public boolean isMessageWellFormed(@Nonnull IRCMessage message) {
        return (!message.isPrefixSet() && message.isParametersExactlyExpectedLength(1));
    }

    @Override
    public String getCommandID() {
        return MessageCodes.PONG;
    }
}
