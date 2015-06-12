package engineer.carrot.warren.warren.irc.messages.core;

import engineer.carrot.warren.warren.irc.messages.AbstractMessage;
import engineer.carrot.warren.warren.irc.messages.IrcMessage;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;

public class PongMessage extends AbstractMessage {
    private String pongAuthor;
    private String pongToken;

    public PongMessage() {

    }

    public PongMessage(String pongToken) {
        this.pongToken = pongToken;
    }

    @Override
    public IrcMessage buildServerOutput() {
        return new IrcMessage.Builder().command(this.getCommandID()).parameters(this.pongToken).build();
    }

    @Override
    public void populateFromIRCMessage(IrcMessage message) {
        this.pongAuthor = message.parameters.get(0);
        this.pongToken = message.parameters.get(1);
    }

    @Override
    public boolean isMessageWellFormed(IrcMessage message) {
        return (message.isParametersAtLeastExpectedLength(2));
    }

    @Override
    public String getCommandID() {
        return MessageCodes.PONG;
    }
}
